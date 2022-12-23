package net.gunivers.dispenser.observer.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.gunivers.dispenser.observer.NbtType;

import java.io.IOException;
import java.util.regex.Pattern;

public class NbtTypeSerializer extends StdSerializer<NbtType> {

    public NbtTypeSerializer() {
        this(null);
    }

    protected NbtTypeSerializer(Class<NbtType> t) {
        super(t);
    }

    @Override
    public void serialize(NbtType nbtType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final String result = Pattern.compile("(?:^|_)([a-z])")
                .matcher(nbtType.name().toLowerCase())
                .replaceAll(m -> m.group(1).toUpperCase())
                .replace("_", "");
        jsonGenerator.writeString(result);
    }
}
