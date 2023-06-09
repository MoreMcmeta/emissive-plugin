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

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Function;

/**
 * From a list of original {@link BakedQuad}s, produces a list of overlay quads that should be rendered
 * overtop the original quads.
 * @author soir20
 */
public class OverlayQuadFunction implements Function<List<BakedQuad>, List<BakedQuad>> {
    private final ModelManager MODEL_MANAGER;

    /**
     * Creates a new quad function.
     */
    public OverlayQuadFunction() {
        MODEL_MANAGER = Minecraft.getInstance().getModelManager();
    }

    @Override
    public List<BakedQuad> apply(List<BakedQuad> quads) {
        return quads.stream()
                .filter(
                        (quad) -> MetadataRegistry.INSTANCE
                                .metadataFromSpriteName(ModConstants.MOD_ID, quad.getSprite().getName())
                                .isPresent()
                ).map(
                        (quad) -> {
                            OverlayMetadata metadata = ((OverlayMetadata) MetadataRegistry.INSTANCE
                                    .metadataFromSpriteName(ModConstants.MOD_ID, quad.getSprite().getName())
                                    .orElseThrow());

                            TextureAtlasSprite sprite = MODEL_MANAGER
                                    .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                                    .getSprite(metadata.overlaySpriteName());

                            /* We have to cast because Minecraft's BakedModel interface expects a List<BakedQuad>,
                               not List<? extends BakedQuad>. */
                            return (BakedQuad) new OverlayBakedQuad(
                                    makeOverlayVertexData(
                                            quad.getVertices(),
                                            sprite,
                                            quad.getSprite(),
                                            metadata.isEmissive()
                                    ),
                                    quad.getTintIndex(),
                                    quad.getDirection(),
                                    sprite,
                                    false,
                                    metadata.isEmissive()
                            );

                        }
                )
                .toList();
    }

    /**
     * Recomputes vertex data for overlay quads.
     * @param vertexData        original vertex data
     * @param newSprite         sprite that will be used as the overlay
     * @param oldSprite         sprite that will have an overlay applied
     * @param emissive          whether the overlay quads should be emissive
     * @return new vertex data for the overlay quads
     */
    private static int[] makeOverlayVertexData(int[] vertexData, TextureAtlasSprite newSprite,
                                               TextureAtlasSprite oldSprite, boolean emissive) {
        final int VERTEX_SIZE = 8;
        final int TEX_U_OFFSET = 4;
        final int TEX_V_OFFSET = 5;
        final int LIGHT_OFFSET = 6;

        int[] newVertexData = new int[vertexData.length];
        System.arraycopy(vertexData, 0, newVertexData, 0, vertexData.length);

        for (int vertex = 0; vertex < 4; vertex++) {
            int vertexOffset = vertex * VERTEX_SIZE;
            int texUOffset = vertexOffset + TEX_U_OFFSET;
            int texVOffset = vertexOffset + TEX_V_OFFSET;
            newVertexData[texUOffset] = recomputeSpriteU(newVertexData[texUOffset], oldSprite, newSprite);
            newVertexData[texVOffset] = recomputeSpriteV(newVertexData[texVOffset], oldSprite, newSprite);
            if (emissive) {
                newVertexData[vertexOffset + LIGHT_OFFSET] = LightTexture.FULL_BRIGHT;
            }
        }

        return newVertexData;
    }

    /**
     * Computes the new u-coordinate for the overlay sprite.
     * @param u             u-coordinate in the original sprite
     * @param oldSprite     sprite that will have an overlay applied
     * @param newSprite     sprite that will be used as the overlay
     * @return equivalent u-coordinate for the overlay sprite
     */
    private static int recomputeSpriteU(int u, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
        return Float.floatToRawIntBits(
                recomputeSpriteCoordinate(
                        Float.intBitsToFloat(u),
                        oldSprite,
                        newSprite,
                        TextureAtlasSprite::getU0,
                        TextureAtlasSprite::getU1
                )
        );
    }

    /**
     * Computes the new v-coordinate for the overlay sprite.
     * @param v             v-coordinate in the original sprite
     * @param oldSprite     sprite that will have an overlay applied
     * @param newSprite     sprite that will be used as the overlay
     * @return equivalent v-coordinate for the overlay sprite
     */
    private static int recomputeSpriteV(int v, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
        return Float.floatToRawIntBits(
                recomputeSpriteCoordinate(
                        Float.intBitsToFloat(v),
                        oldSprite,
                        newSprite,
                        TextureAtlasSprite::getV0,
                        TextureAtlasSprite::getV1
                )
        );
    }

    /**
     * Given a coordinate in the old sprite, recomputes that coordinate to be in the same location in the
     * new sprite (with respect to the size of the new sprite).
     * @param coord         coordinate to recompute
     * @param oldSprite     old sprite with the coordinate
     * @param newSprite     new sprite, which may be a different size, to recompute the coordinate for
     * @param getCoord0     retrieves the first coordinate (u or v) of a sprite
     * @param getCoord1     retrieves the second coordinate (u or v) of a sprite
     * @return recomputed coordinate for the new sprite
     */
    private static float recomputeSpriteCoordinate(float coord, TextureAtlasSprite oldSprite,
                                                   TextureAtlasSprite newSprite,
                                                   Function<TextureAtlasSprite, Float> getCoord0,
                                                   Function<TextureAtlasSprite, Float> getCoord1) {
        float oldCoord0 = getCoord0.apply(oldSprite);
        float oldCoord1 = getCoord1.apply(oldSprite);
        float proportionInSprite = (coord - oldCoord0) / (oldCoord1 - oldCoord0);
        return Mth.lerp(proportionInSprite, getCoord0.apply(newSprite), getCoord1.apply(newSprite));
    }

}
