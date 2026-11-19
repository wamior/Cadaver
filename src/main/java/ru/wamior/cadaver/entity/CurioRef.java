package ru.wamior.cadaver.entity;

import net.minecraft.nbt.CompoundTag;

/** Where a stack sat in the Curios inventory: slot type id (e.g. "ring"), index, cosmetic or not. */
public record CurioRef(String slotId, int index, boolean cosmetic) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", slotId);
        tag.putInt("Index", index);
        tag.putBoolean("Cosmetic", cosmetic);
        return tag;
    }

    public static CurioRef load(CompoundTag tag) {
        return new CurioRef(tag.getString("Id"), tag.getInt("Index"), tag.getBoolean("Cosmetic"));
    }
}
