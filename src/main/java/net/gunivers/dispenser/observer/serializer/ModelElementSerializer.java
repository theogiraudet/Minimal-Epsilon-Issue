package net.gunivers.dispenser.observer.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.gunivers.dispenser.observer.RegistryExporter;
import net.gunivers.dispenser.observer.model.ModelElement;

import java.io.IOException;
import java.util.Optional;

public class ModelElementSerializer extends StdSerializer<ModelElement> implements ContextualSerializer {

    private transient boolean asPath;
    private final transient JsonSerializer<Object> defaultSerializer;

    public ModelElementSerializer() {
        super(ModelElement.class);
        this.defaultSerializer = null;
        this.asPath = true;
    }

    public ModelElementSerializer(JsonSerializer<Object> serializer) {
        super(ModelElement.class);
        this.defaultSerializer = serializer;
    }

    public ModelElementSerializer(JsonSerializer<Object> serializer, boolean asPath) {
        super(ModelElement.class);
        this.defaultSerializer = serializer;
        this.asPath = asPath;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null && beanProperty.getAnnotation(AsPath.class) != null)
                return new ModelElementSerializer(defaultSerializer, true);
        return new ModelElementSerializer(defaultSerializer, false);
    }

    @Override
    public void serialize(ModelElement modelElement, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (asPath) {
            final String root = Optional.ofNullable(modelElement.getClass().getAnnotation(Namespace.class)).map(Namespace::value).orElse("");
            final var uriOpt = RegistryExporter.computeUri(modelElement, root);
            if (uriOpt.isPresent())
                jsonGenerator.writeString(uriOpt.get());
        } else {
            defaultSerializer.serialize(modelElement, jsonGenerator, serializerProvider);
        }
    }
}
