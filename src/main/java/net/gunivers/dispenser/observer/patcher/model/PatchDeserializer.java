package net.gunivers.dispenser.observer.patcher.model;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.*;

public class PatchDeserializer extends StdDeserializer<PatchModel.PatchDocument> {

    public PatchDeserializer() {
        this(null);
    }

    protected PatchDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PatchModel.PatchDocument deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final Map<String, PatchModel.CategoryPatch> resourcePatches = new HashMap<>();
        final var iterator = node.fields();
        while(iterator.hasNext()) {
            final var entry = iterator.next();
            resourcePatches.put(entry.getKey(), new PatchModel.CategoryPatch(getResourcePatches(entry.getValue())));
        }
        return new PatchModel.PatchDocument(resourcePatches);
    }

    private Map<String, PatchModel.ResourcePatch> getResourcePatches(JsonNode node) {
        final Map<String, PatchModel.ResourcePatch> patches = new HashMap<>();
        final var iterator = node.fields();
        while(iterator.hasNext()) {
            final var entry = iterator.next();
            patches.put(entry.getKey(), new PatchModel.ResourcePatch(getPatches(entry.getValue())));
        }
        return patches;
    }

    private Set<PatchModel.Patch> getPatches(JsonNode node) {
        final Set<PatchModel.Patch> patches = new HashSet<>();
        final var iterator = node.fields();
        while(iterator.hasNext()) {
            final var entry = iterator.next();
            getValue(entry.getValue()).ifPresent( value -> patches.add(new PatchModel.Patch(entry.getKey(), value)));
        }
        return patches;
    }

    private Optional<PatchModel.Value> getValue(JsonNode node) {
        final PatchModel.Value value;
        if(node.isTextual())
            value = new PatchModel.StringValue(node.asText());
        else if(node.isContainerNode())
            value = new PatchModel.CompoundValue(getCompoundEntries(node));
        else
            value = null;
        return Optional.ofNullable(value);
    }

    private Map<String, PatchModel.Value> getCompoundEntries(JsonNode node) {
        final Map<String, PatchModel.Value> entries = new HashMap<>();
        final var iterator = node.fields();
        while(iterator.hasNext()) {
            final var entry = iterator.next();
            getValue(entry.getValue()).ifPresent( value -> entries.put(entry.getKey(), value));
        }
        return entries;
    }
}
