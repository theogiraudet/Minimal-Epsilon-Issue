package net.gunivers.dispenser.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.gunivers.dispenser.observer.model.ModelElement;
import net.gunivers.dispenser.observer.serializer.ModelElementBeanSerializerModifier;
import net.gunivers.dispenser.observer.serializer.Namespace;
import net.gunivers.dispenser.observer.serializer.RegistrySerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;


public class RegistryFileExporter implements RegistryExporter {

    private final ObjectMapper mapper;
    private final String rootPath;

    public RegistryFileExporter(String rootPath) {
        this.rootPath = rootPath;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new Jdk8Module());
        final var module = new SimpleModule();
        module.setSerializerModifier(new ModelElementBeanSerializerModifier());
        module.addSerializer(new RegistrySerializer());
        mapper.registerModule(module);
        mapper.registerModule(new Jdk8Module());
    }

    @Override
    public <T extends ModelElement> void export(Registry<T> registry) throws IOException {
        final var identifier = registry.getIdentifier();
        Files.createDirectories(Path.of(computeRegistryPath(identifier, identifier)).getParent().resolve(identifier));
        mapper.writeValue(new File(computeRegistryPath(identifier, identifier)), registry);
        for(final var element : registry.getRegisteredElements().entrySet()) {
            final var path = computeElementPath(element.getValue(), registry.getIdentifier(), element.getKey());
                mapper.writeValue(new File(path), element.getValue());
            }
    }

    private String computeRegistryPath(String identifier, String name) {
        return Path.of(this.rootPath).resolve(identifier).resolve(name + ".json").toString();
    }

    private String computeElementPath(ModelElement element, String identifier, String name) {
        final String namespace = Optional.ofNullable(element.getClass().getAnnotation(Namespace.class)).map(Namespace::value).orElse("");
        return Path.of(this.rootPath).resolve(identifier).resolve(identifier).resolve(namespace).resolve(name + ".json").toString();
    }
}
