package ru.wamior.cadaver.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

public class CorpseInventory extends SimpleContainer {
    private final CorpseEntity corpse;

    public CorpseInventory(CorpseEntity corpse) {
        super(CorpseLayout.SIZE);
        this.corpse = corpse;
    }

    @Override
    public boolean stillValid(Player player) {
        return corpse.isAlive() && player.distanceToSqr(corpse) < 64.0D;
    }

    @Override
    public void stopOpen(Player player) {
        super.stopOpen(player);
        corpse.onContentsTaken();
    }
}
