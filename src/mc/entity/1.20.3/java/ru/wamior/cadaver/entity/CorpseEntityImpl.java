package ru.wamior.cadaver.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/** Minecraft 1.20.3 – 1.20.4 overrides of {@link CorpseEntity}. */
public class CorpseEntityImpl extends CorpseEntity {
    public CorpseEntityImpl(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER_ID, "");
        entityData.define(OWNER_NAME, "");
        entityData.define(SKIN_VALUE, "");
        entityData.define(SKIN_SIGNATURE, "");
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
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
