package net.gunivers.dispenser.observer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.gunivers.dispenser.observer.raw_model.RawJsonBlocks;

import java.io.IOException;
import java.util.Map;

public class BlockDeserializer extends StdDeserializer<RawJsonBlocks.RawBlocks> {

    protected BlockDeserializer(Class<?> vc) {
        super(vc);
    }

    public BlockDeserializer() {
        this(null);
    }

    @Override
    public RawJsonBlocks.RawBlocks deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final JavaType type = TypeFactory.defaultInstance().constructType(new TypeReference<Map<String, RawJsonBlocks.RawBlockProperties>>() {});
        final var map = deserializationContext.<Map<String, RawJsonBlocks.RawBlockProperties>>readValue(jsonParser, type);
        // TODO Transform states
        return new RawJsonBlocks.RawBlocks(map);
    }
}
