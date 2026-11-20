package ru.wamior.cadaver.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.wamior.cadaver.version.Mc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Corpse NBT format, Minecraft 1.20.1 – 1.21.4 (plain CompoundTag getters). */
final class CorpseData {
    static void write(CompoundTag tag, CorpseState state, Level level) {
        state.owner().ifPresent(id -> tag.putUUID("Owner", id));
        tag.putString("OwnerName", state.ownerName());
        tag.putString("SkinValue", state.skinValue());
        tag.putString("SkinSignature", state.skinSignature());
        if (state.deathTime() != null) tag.putLong("DeathTime", state.deathTime());
        tag.putInt("Layout", state.layout());
        tag.putInt("CurioCount", state.curioCount());
        tag.putInt("OtherCount", state.otherCount());
        ListTag refs = new ListTag();
        state.curioRefs().forEach((slot, ref) -> {
            CompoundTag r = new CompoundTag();
            r.putString("Id", ref.slotId());
            r.putInt("Index", ref.index());
            r.putBoolean("Cosmetic", ref.cosmetic());
            r.putInt("Slot", slot);
            refs.add(r);
        });
        tag.put("Curios", refs);
        Mc.saveItems(tag, state.items(), level);
    }

    static CorpseState read(CompoundTag tag, Level level) {
        int layout = tag.getInt("Layout");
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseState.itemSlots(layout), ItemStack.EMPTY);
        Mc.loadItems(tag, items, level);
        Map<Integer, CurioRef> refs = new LinkedHashMap<>();
        ListTag list = tag.getList("Curios", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag r = list.getCompound(i);
            refs.put(r.getInt("Slot"), new CurioRef(r.getString("Id"), r.getInt("Index"), r.getBoolean("Cosmetic")));
        }
        return new CorpseState(
                tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty(),
                tag.getString("OwnerName"), tag.getString("SkinValue"), tag.getString("SkinSignature"),
                tag.contains("DeathTime") ? tag.getLong("DeathTime") : null,
                layout, tag.getInt("CurioCount"), tag.getInt("OtherCount"), refs, items);
    }

    private CorpseData() {
    }
}
