package ru.wamior.cadaver.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import ru.wamior.cadaver.entity.CorpseEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves the skin of a corpse owner: online player info first, then the textures saved at death. */
public final class CorpseSkins {
    public record Skin(ResourceLocation texture, boolean slim) {
        static Skin fallback(UUID id) {
            return new Skin(DefaultPlayerSkin.getDefaultSkin(id), "slim".equals(DefaultPlayerSkin.getSkinModelName(id)));
        }
    }

    private static final Map<UUID, Skin> CACHE = new ConcurrentHashMap<>();

    public static Skin get(CorpseEntity corpse) {
        UUID id = corpse.getOwnerId().orElse(null);
        if (id == null) return Skin.fallback(new UUID(0L, 0L));

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(id);
            if (info != null) return new Skin(info.getSkinLocation(), "slim".equals(info.getModelName()));
        }

        return CACHE.computeIfAbsent(id, key -> load(mc, corpse, key));
    }

    /** Decodes the textures saved at death (no network call) and registers the skin download. */
    private static Skin load(Minecraft mc, CorpseEntity corpse, UUID id) {
        String value = corpse.getSkinValue();
        if (value.isEmpty()) return Skin.fallback(id);
        GameProfile profile = new GameProfile(id, corpse.getOwnerName());
        String signature = corpse.getSkinSignature();
        profile.getProperties().put("textures", new Property("textures", value, signature.isEmpty() ? null : signature));
        SkinManager skins = mc.getSkinManager();
        MinecraftProfileTexture texture = skins.getInsecureSkinInformation(profile).get(MinecraftProfileTexture.Type.SKIN);
        if (texture == null) return Skin.fallback(id);
        ResourceLocation location = skins.registerTexture(texture, MinecraftProfileTexture.Type.SKIN);
        return new Skin(location, "slim".equals(texture.getMetadata("model")));
    }

    private CorpseSkins() {
    }
}
