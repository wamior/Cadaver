package ru.wamior.cadaver.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;

/** Minecraft 1.20.1 overrides of {@link CorpseEntity}. */
public class CorpseEntityImpl extends CorpseEntity {
    public CorpseEntityImpl(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER_ID, Optional.empty());
        entityData.define(OWNER_NAME, "");
        entityData.define(SKIN_VALUE, "");
        entityData.define(SKIN_SIGNATURE, "");
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }
}
