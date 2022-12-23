package net.gunivers.dispenser.observer;

import net.gunivers.dispenser.observer.model.Block;
import net.gunivers.dispenser.observer.model.BlockState;
import net.gunivers.dispenser.observer.model.EntityType;
import net.gunivers.dispenser.observer.model.ModelElement;
import net.gunivers.dispenser.observer.extractor.BlockExtractor;
import net.gunivers.dispenser.observer.extractor.EntityTypeExtractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Registries {

    public static final Map<String, Registry<? extends ModelElement>> ALL;
    
    public static final Registry<Block> BLOCK_REGISTRY = Registry.create("block", Block.class).extractorProvider(BlockExtractor::new).build();
    public static final Registry<BlockState> BLOCK_STATE_REGISTRY = Registry.create("block_state", BlockState.class).build();
    public static final Registry<EntityType> ENTITY_REGISTRY = Registry.create("entity", EntityType.class).extractorProvider(EntityTypeExtractor::new).build();

    static {
        ALL = new HashMap<>();
        put(BLOCK_REGISTRY);
        put(BLOCK_STATE_REGISTRY);
        put(ENTITY_REGISTRY);
    }

    private static void put(Registry<? extends ModelElement> registry) {
        ALL.put(registry.getIdentifier(), registry);
    }

    public static Optional<String> getRegistryIdFromClass(Class<? extends ModelElement> clazz) {
        return ALL.values().stream()
                .filter(registry -> registry.getResourceClass().equals(clazz))
                .map(Registry::getIdentifier)
                .findFirst();
    }

    private Registries() {}
}
