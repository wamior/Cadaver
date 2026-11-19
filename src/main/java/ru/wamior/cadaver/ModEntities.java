package ru.wamior.cadaver;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.wamior.cadaver.entity.CorpseEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Cadaver.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CorpseEntity>> CORPSE = ENTITY_TYPES.register("corpse",
            () -> EntityType.Builder.<CorpseEntity>of(CorpseEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.4F)
                    .fireImmune()
                    .noSummon()
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build("corpse"));

    private ModEntities() {
    }
}
