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

package io.github.moremcmeta.emissiveplugin.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

/**
 * Searches for sprites that represent base textures (the original texture underneath the overlay).
 * @author soir20
 */
public class BaseSpriteFinder {
    private final TreeMap<UV, TextureAtlasSprite> SPRITES;

    /**
     * Creates a new sprite finder that contains the given sprites. None of the sprites should contain the other
     * sprite, as they will be treated as duplicates and only one will be used.
     * @param sprites       sprites to put in the finder
     */
    public BaseSpriteFinder(Collection<TextureAtlasSprite> sprites) {
        requireNonNull(sprites, "Sprites cannot be null");
        SPRITES = new TreeMap<>();
        sprites.forEach((sprite) -> SPRITES.put(
                new UV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()),
                sprite
        ));
    }

    /**
     * Finds the sprite that contains the provided UV point. This method will return an empty {@link Optional}
     * if there is no sprite that contains the given point, or if it is ambiguous which sprite contains the point
     * (if the point is on the edge or corner of a sprite). Hence, it is best to provide the centroid of a region
     * inside the sprite.
     * @param u     u-coordinate that the sprite must contain
     * @param v     v-coordinate that the sprite must contain
     * @return the sprite if found, or else an empty {@link Optional}
     */
    public Optional<TextureAtlasSprite> find(float u, float v) {
        return Optional.ofNullable(SPRITES.get(new UV(u, v)));
    }

    /**
     * Represents a region or point in the UV plane.
     * @author soir20
     */
    private static class UV implements Comparable<UV> {
        private final float U0;
        private final float U1;
        private final float V0;
        private final float V1;

        /**
         * Creates a UV region.
         * @param u0    first u-coordinate of the region
         * @param v0    first v-coordinate of the region
         * @param u1    second u-coordinate of the region
         * @param v1    second v-coordinate of the region
         */
        public UV(float u0, float v0, float u1, float v1) {
            U0 = Math.min(u0, u1);
            V0 = Math.min(v0, v1);
            U1 = Math.max(u0, u1);
            V1 = Math.max(v0, v1);
        }

        /**
         * Creates a UV point that is the centroid of a UV region.
         * @param centroidU     u-coordinate of the centroid
         * @param centroidV     v-coordinate of the centroid
         */
        public UV(float centroidU, float centroidV) {
            U0 = centroidU;
            U1 = centroidU;
            V0 = centroidV;
            V1 = centroidV;
        }

        @Override
        public int compareTo(@NotNull BaseSpriteFinder.UV other) {
            if (U1 <= other.U0) {
                return -1;
            }

            if (U0 >= other.U1) {
                return 1;
            }

            if (V1 <= other.V0) {
                return -1;
            }

            if (V0 >= other.V1) {
                return 1;
            }

            return 0;
        }
    }

}
