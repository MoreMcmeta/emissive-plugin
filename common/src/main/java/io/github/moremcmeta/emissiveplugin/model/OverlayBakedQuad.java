/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.emissiveplugin.model;

import io.github.moremcmeta.emissiveplugin.metadata.TransparencyMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * A quad that overlays another quad.
 * @author soir20
 */
public final class OverlayBakedQuad extends BakedQuad {
    private final boolean EMISSIVE;
    private final TransparencyMode TRANSPARENCY_MODE;

    /**
     * Creates a new overlay quad.
     * @param vertices          quad vertex data
     * @param tintIndex         index to use to tint this quad
     * @param direction         direction the quad faces
     * @param sprite            sprite to use to texture this quad
     * @param emissive          whether to make this quad emissive
     * @param transparencyMode  transparency mode of the quad
     */
    public OverlayBakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite,
                            boolean emissive, TransparencyMode transparencyMode) {
        super(vertices, tintIndex, direction, sprite, !emissive);
        EMISSIVE = emissive;
        TRANSPARENCY_MODE = transparencyMode;
    }

    /**
     * Checks whether this quad is emissive (full-bright).
     * @return whether this quad is emissive
     */
    public boolean isEmissive() {
        return EMISSIVE;
    }

    /**
     * Gets the transparency mode of the quad.
     * @return transparency mode of the quad
     */
    public TransparencyMode transparencyMode() {
        return TRANSPARENCY_MODE;
    }

}
