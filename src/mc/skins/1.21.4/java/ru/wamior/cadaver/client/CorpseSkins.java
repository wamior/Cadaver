package ru.wamior.cadaver.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import ru.wamior.cadaver.entity.CorpseEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** (Minecraft 1.21.4+) Resolves the skin of a corpse owner: online player info first, then the textures saved at death. */
public final class CorpseSkins {
    public record Skin(ResourceLocation texture, boolean slim) {
        static Skin of(PlayerSkin skin) {
            return new Skin(skin.texture(), skin.model() == PlayerSkin.Model.SLIM);
        }
    }

    private static final Map<UUID, Skin> CACHE = new ConcurrentHashMap<>();
    private static final Set<UUID> LOADING = ConcurrentHashMap.newKeySet();

    public static Skin get(CorpseEntity corpse) {
        UUID id = corpse.getOwnerId().orElse(null);
        if (id == null) return Skin.of(DefaultPlayerSkin.get(new UUID(0L, 0L)));

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(id);
            if (info != null) return Skin.of(info.getSkin());
        }

        Skin cached = CACHE.get(id);
        if (cached != null) return cached;

        if (LOADING.add(id)) {
            GameProfile profile = new GameProfile(id, corpse.getOwnerName());
            String value = corpse.getSkinValue();
            if (!value.isEmpty()) {
                String signature = corpse.getSkinSignature();
                profile.getProperties().put("textures", new Property("textures", value, signature.isEmpty() ? null : signature));
            }
            mc.getSkinManager().getOrLoad(profile).whenComplete((skin, err) -> {
                if (skin != null) skin.ifPresent(loaded -> CACHE.put(id, Skin.of(loaded)));
            });
        }
        return Skin.of(DefaultPlayerSkin.get(id));
    }

    private CorpseSkins() {
    }
}
