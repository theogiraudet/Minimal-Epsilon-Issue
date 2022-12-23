package net.gunivers.dispenser.observer.patcher.model;

import java.util.Map;
import java.util.Set;

public final class PatchModel {

    public record PatchDocument(Map<String, CategoryPatch> patches) {}

    public record CategoryPatch(Map<String, ResourcePatch> resources) {}

    public record ResourcePatch(Set<Patch> patches) {}

    public record Patch(String path, Value newValue) {}

    public sealed interface Value permits StringValue, CompoundValue {}

    public record StringValue(String value) implements Value {}

    public record CompoundValue(Map<String, Value> entries) implements Value {}

}
