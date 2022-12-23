package net.gunivers.dispenser.observer;


import net.gunivers.dispenser.observer.model.EntityType;

import java.util.HashSet;
import java.util.Set;

public class NbtFilter {

    public static void removeDuplicate(EntityType entity) {
        removeDuplicateRec(entity);
    }
    private static Set<String> removeDuplicateRec(EntityType entity) {
        final var parentMap = entity.parent().map(NbtFilter::removeDuplicateRec).orElse(new HashSet<>());
        parentMap.forEach(key -> entity.nbt().remove(key));
        parentMap.addAll(entity.nbt().keySet());
        return parentMap;
    }

}
