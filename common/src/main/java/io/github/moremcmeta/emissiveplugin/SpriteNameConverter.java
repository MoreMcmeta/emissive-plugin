package io.github.moremcmeta.emissiveplugin;

import net.minecraft.resources.ResourceLocation;

public class SpriteNameConverter {
    private SpriteNameConverter() {}

    public static ResourceLocation toSpriteName(ResourceLocation textureLocation) {
        int prefixLength = "textures/".length();
        int postfixLength = ".png".length();
        int pathLength = textureLocation.getPath().length();

        return new ResourceLocation(
                textureLocation.getNamespace(),
                textureLocation.getPath().substring(prefixLength, pathLength - postfixLength)
        );
    }

    /**
     * Converts a sprite name to a standard texture location (with textures/ prefix and .png suffix).
     * @param spriteName      the sprite name to convert
     * @return the texture location corresponding to the sprite
     */
    public static ResourceLocation toTextureLocation(ResourceLocation spriteName) {
        String originalPath = spriteName.getPath();
        String fullPath = "textures/" + originalPath + ".png";
        return new ResourceLocation(spriteName.getNamespace(), fullPath);
    }

}
