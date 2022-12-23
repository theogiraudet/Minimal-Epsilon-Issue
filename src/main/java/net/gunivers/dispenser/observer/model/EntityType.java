package net.gunivers.dispenser.observer.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Optional;

public sealed interface EntityType extends ModelElement permits EntityType.ConcreteEntityType, EntityType.AbstractEntityType {

    String className();
    Map<String, NbtTypeValue> nbt();
    Optional<EntityType> parent();

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "className")
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    record ConcreteEntityType(@JsonIdentityReference(alwaysAsId = true) Optional<EntityType> parent, String className, String id, Map<String, NbtTypeValue> nbt) implements EntityType {
        @Override
        public String getId() {
            return id();
        }
    }
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "className")
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    record AbstractEntityType(@JsonIdentityReference(alwaysAsId = true) Optional<EntityType> parent, String className, Map<String, NbtTypeValue> nbt) implements EntityType {
        @Override
        public String getId() {
            return className();
        }
    }

    sealed interface NbtTypeValue permits NbtTypeString, NbtTypeCompound {}

    @JsonSerialize
    record NbtTypeString(@JsonProperty String nbtType) implements NbtTypeValue {

        @JsonCreator
        public static NbtTypeString of(final String value) {
            return new NbtTypeString(value);
        }

        @JsonValue
        public String nbtType() {
            return nbtType;
        }
    }

    @JsonSerialize
    record NbtTypeCompound(@JsonProperty Map<String, NbtTypeValue> entries) implements NbtTypeValue {

        @JsonCreator
        public static NbtTypeCompound of(final Map<String, NbtTypeValue> entries) {
            return new NbtTypeCompound(entries);
        }

        @JsonValue
        public Map<String, NbtTypeValue> entries() {
            return entries;
        }
    }

}
