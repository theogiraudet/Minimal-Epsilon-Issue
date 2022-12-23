package net.gunivers.dispenser.observer.transformer;


import net.gunivers.dispenser.observer.Registries;
import net.gunivers.dispenser.observer.model.Block;
import net.gunivers.dispenser.observer.model.BlockState;
import net.gunivers.dispenser.observer.raw_model.RawJsonBlocks;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockTransformer implements Transformer<RawJsonBlocks.RawBlocks, Set<Block>> {
    @Override
    public Set<Block> transform(RawJsonBlocks.RawBlocks source) {
        final Set<Block> blocks =  source.block2Properties().entrySet().stream()
                .map(entry -> new Block(entry.getKey().replace("minecraft:", ""), transformBlockState(entry.getValue()), transformDefaultProperties(entry.getValue().states())))
                .collect(Collectors.toSet());
        blocks.forEach(Registries.BLOCK_REGISTRY::register);
        return blocks;
    }

    private Set<BlockState> transformBlockState(RawJsonBlocks.RawBlockProperties properties) {
        final Set<BlockState> blockStates = Optional.ofNullable(properties.properties()).orElse(Map.of()).entrySet().stream()
                .map(props -> new BlockState(props.getKey(), props.getValue()))
                .collect(Collectors.toSet());
        blockStates.forEach(Registries.BLOCK_STATE_REGISTRY::register);
        return blockStates;
    }

    private Map<String, String> transformDefaultProperties(Set<RawJsonBlocks.RawBlockPropertiesCombination> combinations) {
        return combinations.stream()
                .filter(RawJsonBlocks.RawBlockPropertiesCombination::isDefault)
                .map(RawJsonBlocks.RawBlockPropertiesCombination::properties)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(Map.of());
    }
}
