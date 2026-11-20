package ru.wamior.cadaver.client;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.version.Profiles;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** (Minecraft 1.21.9+) Resolves the skin of a corpse owner: online player info first, then the textures saved at death. */
public final class CorpseSkins {
    public record Skin(ResourceLocation texture, boolean slim) {
        static Skin of(PlayerSkin skin) {
            return new Skin(skin.body().texturePath(), skin.model() == PlayerModelType.SLIM);
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
            String value = corpse.getSkinValue();
            String signature = corpse.getSkinSignature();
            Property textures = value.isEmpty() ? null : new Property("textures", value, signature.isEmpty() ? null : signature);
            mc.getSkinManager().get(Profiles.withTextures(id, corpse.getOwnerName(), textures)).whenComplete((skin, err) -> {
                if (skin != null) skin.ifPresent(loaded -> CACHE.put(id, Skin.of(loaded)));
            });
        }
        return Skin.of(DefaultPlayerSkin.get(id));
    }

    private CorpseSkins() {
    }
}
