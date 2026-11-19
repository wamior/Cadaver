package ru.wamior.cadaver.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.Optional;

/** Minecraft 1.21.1 overrides of {@link CorpseEntity}. */
public class CorpseEntityImpl extends CorpseEntity {
    public CorpseEntityImpl(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, Optional.empty());
        builder.define(OWNER_NAME, "");
        builder.define(SKIN_VALUE, "");
        builder.define(SKIN_SIGNATURE, "");
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }
}
