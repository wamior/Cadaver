package ru.wamior.cadaver.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.wamior.cadaver.version.Mc;

import java.util.LinkedHashMap;
import java.util.Map;

/** Corpse NBT format, Minecraft 1.21.5 (Optional getters, UUIDs through UUIDUtil.CODEC). Same keys as before. */
final class CorpseData {
    static void write(CompoundTag tag, CorpseState state, Level level) {
        state.owner().ifPresent(id -> tag.store("Owner", UUIDUtil.CODEC, id));
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
        int layout = tag.getIntOr("Layout", 0);
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseState.itemSlots(layout), ItemStack.EMPTY);
        Mc.loadItems(tag, items, level);
        Map<Integer, CurioRef> refs = new LinkedHashMap<>();
        ListTag list = tag.getListOrEmpty("Curios");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag r = list.getCompoundOrEmpty(i);
            refs.put(r.getIntOr("Slot", -1), new CurioRef(r.getStringOr("Id", ""), r.getIntOr("Index", 0), r.getBooleanOr("Cosmetic", false)));
        }
        return new CorpseState(tag.read("Owner", UUIDUtil.CODEC),
                tag.getStringOr("OwnerName", ""), tag.getStringOr("SkinValue", ""), tag.getStringOr("SkinSignature", ""),
                tag.getLong("DeathTime").orElse(null),
                layout, tag.getIntOr("CurioCount", 0), tag.getIntOr("OtherCount", 0), refs, items);
    }

    private CorpseData() {
    }
}
