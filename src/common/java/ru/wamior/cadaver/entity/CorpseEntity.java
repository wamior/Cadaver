package ru.wamior.cadaver.entity;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
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
import ru.wamior.cadaver.version.Chat;
import ru.wamior.cadaver.version.Mc;
import ru.wamior.cadaver.version.Perms;
import ru.wamior.cadaver.version.Profiles;

import net.minecraft.core.NonNullList;
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

    /** Owner UUID as a string ("" = none): the optional-UUID serializer does not exist on every version. */
    protected static final EntityDataAccessor<String> OWNER_ID =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
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
        if (y < Mc.minY(level)) {
            // died in the void: keep the corpse reachable at the bottom of the world
            y = Mc.minY(level) + 1;
            corpse.setNoGravity(true);
        }
        Mc.moveTo(corpse, player.getX(), y, player.getZ(), player.getYRot(), 0.0F);
        corpse.entityData.set(OWNER_ID, player.getUUID().toString());
        corpse.entityData.set(OWNER_NAME, Profiles.name(player.getGameProfile()));
        Property textures = Profiles.textures(player.getGameProfile());
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
        String id = entityData.get(OWNER_ID);
        if (id.isEmpty()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
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
        if (!isOwner(player) && !Perms.isOperator(player)) {
            long left = protectionTicksLeft();
            if (left > 0) {
                long minutes = (left + TICKS_PER_MINUTE - 1) / TICKS_PER_MINUTE;
                Chat.actionBar(player, Component.translatable("message.cadaver.protected", getOwnerName(), minutes));
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
            Chat.actionBar(player, Component.translatable("message.cadaver.inventory_full"));
        }
        onContentsTaken();
    }

    public void onContentsTaken() {
        if (!level().isClientSide() && isAlive() && inventory.isEmpty() && CadaverConfig.removeWhenEmpty()) {
            discard();
        }
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    public void tick() {
        super.tick();

        if (getY() < Mc.minY(level())) {
            setPos(getX(), Mc.minY(level()) + 1, getZ());
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
        if (level().isClientSide() || tickCount % 20 != 0) return;
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
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    // ---------------------------------------------------------------- data

    /** Everything a corpse saves; the per-version {@code CorpseData} turns it into NBT / ValueOutput. */
    public CorpseState saveState() {
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseLayout.SIZE, ItemStack.EMPTY);
        for (int i = 0; i < CorpseLayout.SIZE; i++) items.set(i, inventory.getItem(i));
        Map<Integer, CurioRef> refs = new java.util.LinkedHashMap<>();
        for (int i = 0; i < curioCount; i++) {
            if (curioRefs[i] != null) refs.put(i, curioRefs[i]);
        }
        return new CorpseState(getOwnerId(), getOwnerName(), getSkinValue(), getSkinSignature(), deathGameTime,
                CorpseLayout.VERSION, curioCount, otherCount, refs, items);
    }

    public void loadState(CorpseState state) {
        entityData.set(OWNER_ID, state.owner().map(UUID::toString).orElse(""));
        entityData.set(OWNER_NAME, state.ownerName());
        entityData.set(SKIN_VALUE, state.skinValue());
        entityData.set(SKIN_SIGNATURE, state.skinSignature());
        deathGameTime = state.deathTime() != null ? state.deathTime() : level().getGameTime();
        inventory.clearContent();
        java.util.Arrays.fill(curioRefs, null);
        if (state.layout() >= CorpseLayout.VERSION) {
            for (int i = 0; i < CorpseLayout.SIZE && i < state.items().size(); i++) inventory.setItem(i, state.items().get(i));
            curioCount = Math.min(state.curioCount(), CorpseLayout.EXTRA_COUNT);
            otherCount = Math.min(state.otherCount(), CorpseLayout.EXTRA_COUNT - curioCount);
            state.curioRefs().forEach((slot, ref) -> {
                if (slot >= 0 && slot < curioCount) curioRefs[slot] = ref;
            });
        } else {
            loadLegacy(state.items());
        }
    }

    /** Corpses saved by 1.0.0 used a 54-slot chest layout with extras mixed in. */
    private void loadLegacy(List<ItemStack> old) {
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
