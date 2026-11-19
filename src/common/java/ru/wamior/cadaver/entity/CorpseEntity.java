package ru.wamior.cadaver.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.wamior.cadaver.CadaverConfig;
import ru.wamior.cadaver.platform.Platform;
import ru.wamior.cadaver.version.Mc;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import ru.wamior.cadaver.compat.AccessoryCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A player's corpse. Deliberately invisible to every raycast (crosshair, arrows, modded bullets,
 * melee) – it is opened through our own client look check and a C2S packet instead
 * ({@link ru.wamior.cadaver.network.OpenCorpse}).
 * <p>
 * The few overrides whose signatures differ between Minecraft versions live in the per-version
 * {@code CorpseEntityImpl} (src/mc-&lt;version&gt;), the only concrete subclass.
 */
public abstract class CorpseEntity extends Entity {
    private static final long TICKS_PER_MINUTE = 1200L;

    protected static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> SKIN_VALUE =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> SKIN_SIGNATURE =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);

    private final CorpseInventory inventory = new CorpseInventory(this);
    private final CurioRef[] curioRefs = new CurioRef[CorpseLayout.EXTRA_COUNT];
    private int curioCount;
    private int otherCount;
    private long deathGameTime;

    private CurioRef curioRefAt(int index) {
        int extra = index - CorpseLayout.EXTRA;
        return extra >= 0 && extra < curioCount ? curioRefs[extra] : null;
    }

    protected CorpseEntity(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
        setInvulnerable(true);
    }

    public static CorpseEntity create(ServerPlayer player) {
        Level level = player.level();
        CorpseEntity corpse = new CorpseEntityImpl(Platform.corpseType(), level);
        double y = player.getY();
        if (y < level.getMinBuildHeight()) {
            // died in the void: keep the corpse reachable at the bottom of the world
            y = level.getMinBuildHeight() + 1;
            corpse.setNoGravity(true);
        }
        corpse.moveTo(player.getX(), y, player.getZ(), player.getYRot(), 0.0F);
        corpse.entityData.set(OWNER_ID, Optional.of(player.getUUID()));
        corpse.entityData.set(OWNER_NAME, player.getGameProfile().getName());
        Property textures = Iterables.getFirst(player.getGameProfile().getProperties().get("textures"), null);
        if (textures != null) {
            String signature = Mc.textureSignature(textures);
            corpse.entityData.set(SKIN_VALUE, Mc.textureValue(textures));
            corpse.entityData.set(SKIN_SIGNATURE, signature == null ? "" : signature);
        }
        corpse.deathGameTime = level.getGameTime();
        return corpse;
    }

    // ---------------------------------------------------------------- items

    public CorpseInventory getInventory() {
        return inventory;
    }

    /**
     * Puts a stack into the slot it had in the player's inventory (armor, offhand, main, hotbar).
     * @return false if it has no such slot or the slot is taken – the caller treats it as an extra
     */
    public boolean addToPlayerSlot(ItemStack stack, int playerSlot) {
        int index = CorpseLayout.corpseIndex(playerSlot);
        if (index >= 0 && inventory.getItem(index).isEmpty()) {
            inventory.setItem(index, stack);
            return true;
        }
        return false;
    }

    /**
     * Fills the extra section: Curios first (remembering their slots), then other drops.
     * Call once, after {@link #addToPlayerSlot}. Returns what didn't fit.
     */
    public List<ItemStack> fillExtras(List<Map.Entry<ItemStack, CurioRef>> curios, List<ItemStack> others) {
        List<ItemStack> overflow = new ArrayList<>();
        int next = 0;
        for (Map.Entry<ItemStack, CurioRef> curio : curios) {
            if (next < CorpseLayout.EXTRA_COUNT) {
                inventory.setItem(CorpseLayout.EXTRA + next, curio.getKey());
                curioRefs[next] = curio.getValue();
                next++;
            } else {
                overflow.add(curio.getKey());
            }
        }
        curioCount = next;
        for (ItemStack stack : others) {
            if (next < CorpseLayout.EXTRA_COUNT) {
                inventory.setItem(CorpseLayout.EXTRA + next++, stack);
            } else {
                overflow.add(stack);
            }
        }
        otherCount = next - curioCount;
        return overflow;
    }

    public int getCurioCount() {
        return curioCount;
    }

    public int getOtherCount() {
        return otherCount;
    }

    // ---------------------------------------------------------------- owner

    public Optional<UUID> getOwnerId() {
        return entityData.get(OWNER_ID);
    }

    public String getOwnerName() {
        return entityData.get(OWNER_NAME);
    }

    public String getSkinValue() {
        return entityData.get(SKIN_VALUE);
    }

    public String getSkinSignature() {
        return entityData.get(SKIN_SIGNATURE);
    }

    public boolean isOwner(ServerPlayer player) {
        return getOwnerId().map(player.getUUID()::equals).orElse(false);
    }

    private long protectionTicksLeft() {
        long protection = CadaverConfig.ownerProtectionMinutes() * TICKS_PER_MINUTE;
        return Math.max(0L, protection - (level().getGameTime() - deathGameTime));
    }

    // ---------------------------------------------------------------- interaction

    public void interact(ServerPlayer player, boolean quickLoot) {
        if (!isAlive()) return;
        if (!isOwner(player) && !player.hasPermissions(2)) {
            long left = protectionTicksLeft();
            if (left > 0) {
                long minutes = (left + TICKS_PER_MINUTE - 1) / TICKS_PER_MINUTE;
                player.displayClientMessage(Component.translatable("message.cadaver.protected", getOwnerName(), minutes), true);
                return;
            }
        }
        if (quickLoot && CadaverConfig.quickLoot()) {
            quickLoot(player);
        } else {
            Platform.openCorpseMenu(player, Component.translatable("container.cadaver.corpse", getOwnerName()),
                    inventory, curioCount, otherCount);
        }
    }

    private void quickLoot(ServerPlayer player) {
        Inventory inv = player.getInventory();
        boolean owner = isOwner(player);
        if (owner) {
            // first pass: restore everything to its original slot (armor, offhand, hotbar, curios)
            for (int i = 0; i < CorpseLayout.SIZE; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;
                int slot = CorpseLayout.playerSlot(i);
                if (slot >= 0 && inv.getItem(slot).isEmpty()) {
                    inv.setItem(slot, stack);
                    inventory.setItem(i, ItemStack.EMPTY);
                    continue;
                }
                CurioRef ref = curioRefAt(i);
                if (ref != null && AccessoryCompat.tryRestore(player, ref, stack)) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < CorpseLayout.SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            inv.add(stack);
            inventory.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
        if (!inventory.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.cadaver.inventory_full"), true);
        }
        onContentsTaken();
    }

    public void onContentsTaken() {
        if (!level().isClientSide && isAlive() && inventory.isEmpty() && CadaverConfig.removeWhenEmpty()) {
            discard();
        }
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    public void tick() {
        super.tick();

        if (getY() < level().getMinBuildHeight()) {
            setPos(getX(), level().getMinBuildHeight() + 1, getZ());
            setDeltaMovement(Vec3.ZERO);
            setNoGravity(true);
        }

        if (!isNoGravity()) {
            if (!onGround()) {
                setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(onGround() ? Vec3.ZERO : getDeltaMovement().multiply(0.0D, 0.98D, 0.0D));
        }
    }

    /** Called by ServerLevel every tick for loaded entities. */
    @Override
    public void checkDespawn() {
        if (level().isClientSide || tickCount % 20 != 0) return;
        int minutes = CadaverConfig.despawnMinutes();
        if (minutes > 0 && level().getGameTime() - deathGameTime >= minutes * TICKS_PER_MINUTE) {
            if (CadaverConfig.dropItemsOnDespawn()) {
                Containers.dropContents(level(), blockPosition(), inventory);
            }
            inventory.clearContent();
            discard();
        }
    }

    /** Box used by the client-side look check; covers the body lying along its facing axis. */
    public AABB getInteractionBox() {
        double rad = Math.toRadians(getYRot());
        boolean alongX = Math.abs(Math.sin(rad)) > Math.abs(Math.cos(rad));
        double hx = alongX ? 0.95D : 0.4D;
        double hz = alongX ? 0.4D : 0.95D;
        return new AABB(getX() - hx, getY(), getZ() - hz, getX() + hx, getY() + 0.45D, getZ() + hz);
    }

    // ---------------------------------------------------------------- no hitbox, no damage

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    // ---------------------------------------------------------------- data

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        getOwnerId().ifPresent(id -> tag.putUUID("Owner", id));
        tag.putString("OwnerName", getOwnerName());
        tag.putString("SkinValue", getSkinValue());
        tag.putString("SkinSignature", getSkinSignature());
        tag.putLong("DeathTime", deathGameTime);
        tag.putInt("Layout", CorpseLayout.VERSION);
        tag.putInt("CurioCount", curioCount);
        tag.putInt("OtherCount", otherCount);
        ListTag refs = new ListTag();
        for (int i = 0; i < curioCount; i++) {
            if (curioRefs[i] != null) {
                CompoundTag ref = curioRefs[i].save();
                ref.putInt("Slot", i);
                refs.add(ref);
            }
        }
        tag.put("Curios", refs);
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseLayout.SIZE, ItemStack.EMPTY);
        for (int i = 0; i < CorpseLayout.SIZE; i++) items.set(i, inventory.getItem(i));
        Mc.saveItems(tag, items, level());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        entityData.set(OWNER_NAME, tag.getString("OwnerName"));
        entityData.set(SKIN_VALUE, tag.getString("SkinValue"));
        entityData.set(SKIN_SIGNATURE, tag.getString("SkinSignature"));
        deathGameTime = tag.contains("DeathTime") ? tag.getLong("DeathTime") : level().getGameTime();
        inventory.clearContent();
        java.util.Arrays.fill(curioRefs, null);
        if (tag.getInt("Layout") >= CorpseLayout.VERSION) {
            NonNullList<ItemStack> items = NonNullList.withSize(CorpseLayout.SIZE, ItemStack.EMPTY);
            Mc.loadItems(tag, items, level());
            for (int i = 0; i < CorpseLayout.SIZE; i++) inventory.setItem(i, items.get(i));
            curioCount = Math.min(tag.getInt("CurioCount"), CorpseLayout.EXTRA_COUNT);
            otherCount = Math.min(tag.getInt("OtherCount"), CorpseLayout.EXTRA_COUNT - curioCount);
            ListTag refs = tag.getList("Curios", Tag.TAG_COMPOUND);
            for (int i = 0; i < refs.size(); i++) {
                CompoundTag ref = refs.getCompound(i);
                int slot = ref.getInt("Slot");
                if (slot >= 0 && slot < curioCount) curioRefs[slot] = CurioRef.load(ref);
            }
        } else {
            loadLegacy(tag);
        }
    }

    /** Corpses saved by 1.0.0 used a 54-slot chest layout with extras mixed in. */
    private void loadLegacy(CompoundTag tag) {
        NonNullList<ItemStack> old = NonNullList.withSize(54, ItemStack.EMPTY);
        Mc.loadItems(tag, old, level());
        List<ItemStack> others = new ArrayList<>();
        for (int i = 0; i < old.size(); i++) {
            ItemStack stack = old.get(i);
            if (stack.isEmpty()) continue;
            if (!addToPlayerSlot(stack, CorpseLayout.legacyPlayerSlot(i))) others.add(stack);
        }
        // the old layout had at most 13 extras, so nothing can overflow the 36 extra slots here
        fillExtras(List.of(), others);
    }
}
