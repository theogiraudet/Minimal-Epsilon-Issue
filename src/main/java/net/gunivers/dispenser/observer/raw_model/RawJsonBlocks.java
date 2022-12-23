package net.gunivers.dispenser.observer.raw_model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Set;

public interface RawJsonBlocks {
    @JsonSerialize
    record RawBlocks(Map<String, RawBlockProperties> block2Properties) {}
    @JsonSerialize
    record RawBlockProperties(Map<String, Set<String>> properties, Set<RawBlockPropertiesCombination> states) {}

    record RawBlockPropertiesCombination(String id, boolean isDefault, Map<String, String> properties) {

        public RawBlockPropertiesCombination(@JsonProperty("id") String id, @JsonProperty("properties") Map<String, String> properties) {
            this(id, false, properties);
        }

        public RawBlockPropertiesCombination(@JsonProperty("id") String id, @JsonProperty("default") boolean isDefault, @JsonProperty("properties") Map<String, String> properties) {
            this.id = id;
            this.isDefault = isDefault;
            this.properties = properties;
        }

    }

}