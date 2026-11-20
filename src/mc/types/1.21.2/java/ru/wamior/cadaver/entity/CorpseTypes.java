package ru.wamior.cadaver.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ru.wamior.cadaver.version.Ids;

/** Builds the corpse entity type (Minecraft 1.21.2+); each loader registers it under cadaver:corpse its own way. */
public final class CorpseTypes {
    public static final String CORPSE = "corpse";

    public static EntityType<CorpseEntity> createCorpseType() {
        return EntityType.Builder.<CorpseEntity>of(CorpseEntityImpl::new, MobCategory.MISC)
                .sized(1.0F, 0.4F)
                .fireImmune()
                .noSummon()
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Ids.id(CORPSE)));
    }

    private CorpseTypes() {
    }
}
