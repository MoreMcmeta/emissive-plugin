package io.github.moremcmeta.emissiveplugin.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class OverlayBakedQuad extends BakedQuad {
    private final boolean EMISSIVE;

    public OverlayBakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite,
                            boolean shade, boolean emissive) {
        super(vertices, tintIndex, direction, sprite, shade);
        EMISSIVE = emissive;
    }

    public boolean isEmissive() {
        return EMISSIVE;
    }
}
