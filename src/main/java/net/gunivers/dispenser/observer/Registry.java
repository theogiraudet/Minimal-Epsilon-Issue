package net.gunivers.dispenser.observer;

import com.fasterxml.jackson.annotation.*;
import net.gunivers.dispenser.observer.extractor.Extractor;
import net.gunivers.dispenser.observer.model.ModelElement;
import net.gunivers.dispenser.observer.serializer.AsPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class Registry<T extends ModelElement> {

    private final String identifier;
    @JsonIgnoreProperties
    private final Optional<Supplier<Extractor<T>>> extractorProvider;
    private final Class<T> resourceClass;
    @JsonIgnoreProperties
    private final Map<String, T> registeredElements;

    private static final Logger logger = LoggerFactory.getLogger(Registry.class);
    private Registry(String identifier, Class<T> resourceClass, Optional<Supplier<Extractor<T>>> extractorProvider) {
        this.extractorProvider = extractorProvider;
        this.resourceClass = resourceClass;
        this.identifier = identifier;
        this.registeredElements = new HashMap<>();
    }

    @JsonIgnore
    public Optional<Extractor<T>> getExtractor() {
        return extractorProvider.map(Supplier::get);
    }

    @JsonIgnore
    public Class<T> getResourceClass() {
        return resourceClass;
    }

    public void register(T element) {
        if(registeredElements.containsKey(element.getId()) && element.getId().equals("facing"))
            registeredElements.put(element.getId() + "2", element);
        else if(!registeredElements.containsKey(element.getId()))
            registeredElements.put(element.getId(), element);
        else
            logger.warn("'" + element.getId() + "' already registered.");
    }

    @JsonProperty("elements")
    @AsPath
    public Map<String, @AsPath T> getRegisteredElements() {
        return Map.copyOf(this.registeredElements);
    }

    @JsonIgnore
    public String getIdentifier() {
        return identifier;
    }

    public static <T extends ModelElement> Builder<T> create(String identifier, Class<T> resourceClass) {
        return new Builder<>(identifier, resourceClass);
    }
    public static class Builder<T extends ModelElement> {

        private final String identifier;
        private Supplier<Extractor<T>> extractorProvider;
        private final Class<T> resourceClass;

        public Builder(String identifier, Class<T> resourceClass) {
            this.resourceClass = resourceClass;
            this.identifier = identifier;
        }

        public Builder<T> extractorProvider(Supplier<Extractor<T>> extractorProvider) {
            this.extractorProvider = extractorProvider;
            return this;
        }

        public Registry<T> build() {
            return new Registry<>(identifier, resourceClass, Optional.ofNullable(extractorProvider));
        }

    }
}
