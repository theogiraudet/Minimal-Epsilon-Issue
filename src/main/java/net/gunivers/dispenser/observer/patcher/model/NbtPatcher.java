package net.gunivers.dispenser.observer.patcher.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.gunivers.dispenser.observer.model.EntityType;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NbtPatcher {

    private final PatchModel.PatchDocument patch;

    private static final String NBT_PREFIX = "nbt.";
    private static final String IGNORE_PREFIX = "$";

    private final Logger logger = LoggerFactory.getLogger(NbtPatcher.class);

    public NbtPatcher(InputStream patch) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(PatchModel.PatchDocument.class, new PatchDeserializer());
        mapper.registerModule(module);

        this.patch = mapper.readValue(patch, PatchModel.PatchDocument.class);
    }

    public boolean fixEntities(Collection<EntityType> entities) {
        final PatchModel.CategoryPatch catPatch = this.patch.patches().get("entity");

        if(catPatch == null)
            return false;

        final var entityMap = entities.stream().collect(Collectors.toMap(EntityType::className, entity -> entity));

        for(final var entry : catPatch.resources().entrySet()) {
            final var entityType = entityMap.get(entry.getKey());
            fixEntities(entry.getValue(), entityType);
        }
        return true;
    }

    private void fixEntities(PatchModel.ResourcePatch resourcePatch, EntityType entity) {
        if(entity != null) {
            for(final var patches : resourcePatch.patches()) {
                if(patches.path().startsWith(NBT_PREFIX)) {
                    fixValue(patches.path().substring(NBT_PREFIX.length()), entity.nbt(), patches.newValue());
                } else if(patches.path().startsWith(IGNORE_PREFIX))
                    logger.info("Ignore key '" + patches.path() + "'");
                else
                    logger.error("'" + patches.path() + "' not supported.");
            }
        }
    }

    private void fixValue(String path, Map<String, EntityType.NbtTypeValue> entries, PatchModel.Value patchValue) {
        fixValue(path.split("\\."), 0, new EntityType.NbtTypeCompound(entries), patchValue);
    }
    private Optional<EntityType.NbtTypeValue> fixValue(String[] path, int i, EntityType.NbtTypeValue value, PatchModel.Value patchValue) {
        EntityType.NbtTypeValue newValue = null;

        if(value instanceof EntityType.NbtTypeString && path.length > i) {
            logger.error("Cannot edit NbtTypeString path '" + String.join(".", path) + "'");
        }
        else if(value instanceof EntityType.NbtTypeString)
            newValue = convertValue(patchValue);
        else if(value instanceof EntityType.NbtTypeCompound && path.length == i)
            newValue = convertValue(patchValue);
        else if (value instanceof EntityType.NbtTypeCompound compound) {
            final String pathElement = path[i];
            final EntityType.NbtTypeValue childValue = compound.entries().get(pathElement);
            if(childValue != null) {
                fixValue(path,i + 1, childValue, patchValue)
                    .ifPresent(v -> compound.entries().put(pathElement, v));
            } else {
                logger.error("Cannot edit NbtTypeCompound path '" + String.join(".", path) + "'");
            }
        }

        return Optional.ofNullable(newValue);
    }

    private EntityType.NbtTypeValue convertValue(PatchModel.Value value) {
        return switch(value) {
            case PatchModel.StringValue strValue -> new EntityType.NbtTypeString(strValue.value());
            case PatchModel.CompoundValue compoundValue ->
                            new EntityType.NbtTypeCompound
                                    (compoundValue.entries().entrySet().stream()
                                            .filter(entry -> {
                                                final var ignore = entry.getKey().startsWith(IGNORE_PREFIX);
                                                if(ignore)
                                                    logger.info("Ignore key '" + entry.getKey() + "'");
                                                return !ignore;
                                            })
                                            .map(entry -> Pair.of(entry.getKey(), convertValue(entry.getValue())))
                                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                    );
        };
    }

}
