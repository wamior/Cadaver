package ru.wamior.cadaver.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import ru.wamior.cadaver.entity.CorpseEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves the skin of a corpse owner: online player info first, then the textures saved at death. */
public final class CorpseSkins {
    private static final Map<UUID, PlayerSkin> CACHE = new ConcurrentHashMap<>();
    private static final Set<UUID> LOADING = ConcurrentHashMap.newKeySet();

    public static PlayerSkin get(CorpseEntity corpse) {
        UUID id = corpse.getOwnerId().orElse(null);
        if (id == null) return DefaultPlayerSkin.get(new UUID(0L, 0L));

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(id);
            if (info != null) return info.getSkin();
        }

        PlayerSkin cached = CACHE.get(id);
        if (cached != null) return cached;

        if (LOADING.add(id)) {
            GameProfile profile = new GameProfile(id, corpse.getOwnerName());
            String value = corpse.getSkinValue();
            if (!value.isEmpty()) {
                String signature = corpse.getSkinSignature();
                profile.getProperties().put("textures", new Property("textures", value, signature.isEmpty() ? null : signature));
            }
            mc.getSkinManager().getOrLoad(profile).whenComplete((skin, err) -> {
                if (skin != null) CACHE.put(id, skin);
            });
        }
        return DefaultPlayerSkin.get(id);
    }

    private CorpseSkins() {
    }
}
