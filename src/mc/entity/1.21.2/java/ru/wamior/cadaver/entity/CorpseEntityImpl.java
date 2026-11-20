package ru.wamior.cadaver.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/** Minecraft 1.21.2 – 1.21.5 overrides of {@link CorpseEntity}. */
public class CorpseEntityImpl extends CorpseEntity {
    public CorpseEntityImpl(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, "");
        builder.define(OWNER_NAME, "");
        builder.define(SKIN_VALUE, "");
        builder.define(SKIN_SIGNATURE, "");
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        CorpseData.write(tag, saveState(), level());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        loadState(CorpseData.read(tag, level()));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
