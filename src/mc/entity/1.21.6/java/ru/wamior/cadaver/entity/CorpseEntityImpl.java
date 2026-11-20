package ru.wamior.cadaver.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Minecraft 1.21.6+ overrides of {@link CorpseEntity}. */
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
    protected void addAdditionalSaveData(ValueOutput output) {
        CorpseData.write(output, saveState());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        loadState(CorpseData.read(input));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
