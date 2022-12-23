package net.gunivers.dispenser.observer;

import net.gunivers.dispenser.observer.extractor.BlockExtractor;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static final String path = "E:\\Programmation\\IntelliJ\\Observer\\minecraft_source\\yarn\\namedSrc\\net\\minecraft\\entity";
    private static final String blockPath = "E:\\Programmation\\IntelliJ\\Observer\\minecraft_source\\yarn\\generated\\reports\\blocks.json";
    private static final String exportBlockPath = "E:\\Programmation\\IntelliJ\\Observer\\generated\\";
    private static final String pathPatch = "E:\\Programmation\\IntelliJ\\Observer\\patch.json";

    public static void main(String... args) throws IOException {
        final var extractor = new BlockExtractor();
        final var blocks = extractor.extractFrom(Path.of(blockPath));
        final var exporter = new RegistryFileExporter(exportBlockPath);
        exporter.export(Registries.BLOCK_REGISTRY);
        exporter.export(Registries.BLOCK_STATE_REGISTRY);
        System.out.println(blocks.size());
        /*final var extractor = new NbtExtractor();
        final var result = extractor.extractFrom(Path.of(path));
        final var patcher = new NbtPatcher(new FileInputStream(pathPatch));
        patcher.fixEntities(result);
        result.stream().filter(entity -> entity instanceof EntityType.ConcreteEntityType).forEach(NbtFilter::removeDuplicate);
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new Jdk8Module());
        final var module = new SimpleModule();
        module.addSerializer(NbtType.class, new NbtTypeSerializer());
        objectMapper.registerModule(module);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.writeValue(new File("generated/Entities.json"), result);*/
    }

}
