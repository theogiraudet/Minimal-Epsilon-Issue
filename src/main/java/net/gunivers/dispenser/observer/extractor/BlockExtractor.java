package net.gunivers.dispenser.observer.extractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.gunivers.dispenser.observer.BlockDeserializer;
import net.gunivers.dispenser.observer.model.Block;
import net.gunivers.dispenser.observer.raw_model.RawJsonBlocks;
import net.gunivers.dispenser.observer.transformer.BlockTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public class BlockExtractor implements Extractor<Block> {
    @Override
    public Collection<Block> extractFrom(Path location) throws IOException {
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(RawJsonBlocks.RawBlocks.class, new BlockDeserializer());
        final RawJsonBlocks.RawBlocks rawBlocks = new ObjectMapper()
                .registerModule(simpleModule)
                .readValue(new File(location.toUri()), new TypeReference<>() {});
        final Set<Block> blocks = new BlockTransformer().transform(rawBlocks);
        return blocks;
    }
}
