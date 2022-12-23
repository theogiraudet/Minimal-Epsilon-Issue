package net.gunivers.dispenser.observer.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.gunivers.dispenser.observer.Registry;

import java.io.IOException;

public class RegistrySerializer extends StdSerializer<Registry<?>> {

    private final ModelElementSerializer modelElementSerializer;
    public RegistrySerializer() {
        super(Registry.class, false);
        this.modelElementSerializer = new ModelElementSerializer();
    }

    @Override
    public void serialize(Registry<?> registry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        for(final var entry : registry.getRegisteredElements().entrySet()) {
            jsonGenerator.writeFieldName(entry.getKey());
            modelElementSerializer.serialize(entry.getValue(), jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndObject();
    }
}
