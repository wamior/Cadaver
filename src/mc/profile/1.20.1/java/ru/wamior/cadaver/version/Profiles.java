package ru.wamior.cadaver.version;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.util.UUID;

/** GameProfile access, Minecraft 1.20.1 – 1.21.8 (mutable GameProfile class). */
public final class Profiles {
    public static String name(GameProfile profile) {
        return profile.getName();
    }

    /** The signed "textures" property (skin + cape), or null. */
    public static Property textures(GameProfile profile) {
        return Iterables.getFirst(profile.getProperties().get("textures"), null);
    }

    public static GameProfile withTextures(UUID id, String name, Property textures) {
        GameProfile profile = new GameProfile(id, name);
        if (textures != null) profile.getProperties().put("textures", textures);
        return profile;
    }

    private Profiles() {
    }
}
