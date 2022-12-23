package net.gunivers.dispenser.observer;

import net.gunivers.dispenser.observer.model.ModelElement;

import java.io.IOException;
import java.util.Optional;

public interface RegistryExporter {

    <T extends ModelElement> void export(Registry<T> registry) throws IOException;

    static Optional<String> computeUri(ModelElement element, String root) {
        return computeUri(element, root, element.getId());
    }

    static Optional<String> computeUri(ModelElement element, String root, String id) {
        final var identifier = Registries.getRegistryIdFromClass(element.getClass()).map(idRegistry -> idRegistry.startsWith("/") ? idRegistry.substring(1) : idRegistry);
        return identifier.map(idRegistry -> String.format("%s:%s%s", idRegistry, root, id));
    }

}
