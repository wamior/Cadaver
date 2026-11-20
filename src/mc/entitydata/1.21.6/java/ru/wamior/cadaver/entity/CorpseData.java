package ru.wamior.cadaver.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.LinkedHashMap;
import java.util.Map;

/** Corpse save format, Minecraft 1.21.6+ (ValueOutput / ValueInput). Same keys as the NBT versions. */
final class CorpseData {
    static void write(ValueOutput out, CorpseState state) {
        state.owner().ifPresent(id -> out.store("Owner", UUIDUtil.CODEC, id));
        out.putString("OwnerName", state.ownerName());
        out.putString("SkinValue", state.skinValue());
        out.putString("SkinSignature", state.skinSignature());
        if (state.deathTime() != null) out.putLong("DeathTime", state.deathTime());
        out.putInt("Layout", state.layout());
        out.putInt("CurioCount", state.curioCount());
        out.putInt("OtherCount", state.otherCount());
        ValueOutput.ValueOutputList refs = out.childrenList("Curios");
        state.curioRefs().forEach((slot, ref) -> {
            ValueOutput r = refs.addChild();
            r.putString("Id", ref.slotId());
            r.putInt("Index", ref.index());
            r.putBoolean("Cosmetic", ref.cosmetic());
            r.putInt("Slot", slot);
        });
        ContainerHelper.saveAllItems(out, state.items());
    }

    static CorpseState read(ValueInput in) {
        int layout = in.getIntOr("Layout", 0);
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseState.itemSlots(layout), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);
        Map<Integer, CurioRef> refs = new LinkedHashMap<>();
        for (ValueInput r : in.childrenListOrEmpty("Curios")) {
            refs.put(r.getIntOr("Slot", -1), new CurioRef(r.getStringOr("Id", ""), r.getIntOr("Index", 0), r.getBooleanOr("Cosmetic", false)));
        }
        return new CorpseState(in.read("Owner", UUIDUtil.CODEC),
                in.getStringOr("OwnerName", ""), in.getStringOr("SkinValue", ""), in.getStringOr("SkinSignature", ""),
                in.getLong("DeathTime").orElse(null),
                layout, in.getIntOr("CurioCount", 0), in.getIntOr("OtherCount", 0), refs, items);
    }

    private CorpseData() {
    }
}
