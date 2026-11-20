package ru.wamior.cadaver.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Builds the corpse entity type (Minecraft 1.20.1 – 1.21.1); each loader registers it under cadaver:corpse its own way. */
public final class CorpseTypes {
    public static final String CORPSE = "corpse";

    public static EntityType<CorpseEntity> createCorpseType() {
        return EntityType.Builder.<CorpseEntity>of(CorpseEntityImpl::new, MobCategory.MISC)
                .sized(1.0F, 0.4F)
                .fireImmune()
                .noSummon()
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(CORPSE);
    }

    private CorpseTypes() {
    }
}
