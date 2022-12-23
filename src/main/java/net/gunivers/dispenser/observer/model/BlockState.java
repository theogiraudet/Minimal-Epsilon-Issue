package net.gunivers.dispenser.observer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.gunivers.dispenser.observer.serializer.Namespace;

import java.util.Set;

@Namespace
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record BlockState(String id, Set<String> values) implements ModelElement {
    @Override
    public String getId() {
        return id();
    }
}
