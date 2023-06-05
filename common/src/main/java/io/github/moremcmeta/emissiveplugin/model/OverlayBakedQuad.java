package io.github.moremcmeta.emissiveplugin.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * A quad that overlays another quad.
 * @author soir20
 */
public class OverlayBakedQuad extends BakedQuad {
    private final boolean EMISSIVE;

    /**
     * Creates a new overlay quad.
     * @param vertices      quad vertex data
     * @param tintIndex     index to use to tint this quad
     * @param direction     direction the quad faces
     * @param sprite        sprite to use to texture this quad
     * @param shade         whether to apply shade to this quad
     * @param emissive      whether to make this quad emissive
     */
    public OverlayBakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite,
                            boolean shade, boolean emissive) {
        super(vertices, tintIndex, direction, sprite, shade);
        EMISSIVE = emissive;
    }

    /**
     * Checks whether this quad is emissive (full-bright).
     * @return whether this quad is emissive
     */
    public boolean isEmissive() {
        return EMISSIVE;
    }

}
