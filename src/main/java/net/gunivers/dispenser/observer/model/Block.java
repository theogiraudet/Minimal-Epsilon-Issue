package net.gunivers.dispenser.observer.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.gunivers.dispenser.observer.serializer.AsPath;

import java.util.Map;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record Block(String id, @AsPath Set<BlockState> properties, @JsonProperty("default_properties") Map<String, String> defaultProperties) implements ModelElement {
    @Override
    public String getId() {
        return id();
    }
}
