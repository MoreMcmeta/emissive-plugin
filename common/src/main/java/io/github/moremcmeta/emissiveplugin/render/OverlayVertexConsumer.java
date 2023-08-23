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

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.fabricapi.SpriteFinder;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Only renders vertices that are part of overlay quads for fluids.
 * @author soir20
 */
@MethodsReturnNonnullByDefault
public final class OverlayVertexConsumer implements VertexConsumer {
    private static final int VERTS_PER_QUAD = 4;
    private final SpriteFinder SPRITE_FINDER;
    private final VertexConsumer DELEGATE;
    private final List<Task>[] TASKS;
    private final boolean[] HAS_UV;
    private final float[] TEX_U;
    private final float[] TEX_V;
    private final int[] LIGHT_U;
    private final int[] LIGHT_V;
    private final int[] RED;
    private final int[] GREEN;
    private final int[] BLUE;
    private int currentVertex;

    /**
     * Creates a new buffer.
     * @param spriteFinder      sprite finder to search for sprites
     * @param delegate          delegate buffer to write vertices to
     */
    @SuppressWarnings("unchecked")
    public OverlayVertexConsumer(SpriteFinder spriteFinder, VertexConsumer delegate) {
        SPRITE_FINDER = requireNonNull(spriteFinder, "Sprite finder cannot be null");
        DELEGATE = requireNonNull(delegate, "Delegate cannot be null");

        TASKS = Stream.generate(ArrayList::new).limit(VERTS_PER_QUAD).toArray(List[]::new);
        HAS_UV = new boolean[VERTS_PER_QUAD];
        TEX_U = new float[VERTS_PER_QUAD];
        TEX_V = new float[VERTS_PER_QUAD];
        LIGHT_U = new int[VERTS_PER_QUAD];
        LIGHT_V = new int[VERTS_PER_QUAD];
        RED = new int[VERTS_PER_QUAD];
        GREEN = new int[VERTS_PER_QUAD];
        BLUE = new int[VERTS_PER_QUAD];
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, red, green, blue) -> delegate.vertex(x, y, z));
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, r, g, b) -> delegate.color(r, g, b, alpha));
        RED[currentVertex] = red;
        GREEN[currentVertex] = green;
        BLUE[currentVertex] = blue;
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, red, green, blue) -> delegate.uv(texU, texV));
        HAS_UV[currentVertex] = true;
        TEX_U[currentVertex] = u;
        TEX_V[currentVertex] = v;
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, red, green, blue) -> delegate.overlayCoords(u, v));
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, red, green, blue) -> delegate.uv2(lightU, lightV));
        LIGHT_U[currentVertex] = u;
        LIGHT_V[currentVertex] = v;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, red, green, blue) -> delegate.normal(x, y ,z));
        return this;
    }

    @Override
    public void endVertex() {
        if (currentVertex < VERTS_PER_QUAD - 1) {
            currentVertex++;
            return;
        }

        boolean hasUV = true;
        float centroidU = 0;
        float centroidV = 0;
        for (int vertex = 0; vertex < VERTS_PER_QUAD; vertex++) {
            hasUV = hasUV && HAS_UV[vertex];
            centroidU += TEX_U[vertex];
            centroidV += TEX_V[vertex];
            HAS_UV[vertex] = false;
        }
        centroidU /= VERTS_PER_QUAD;
        centroidV /= VERTS_PER_QUAD;

        // Ignore vertices without an associated texture
        if (hasUV) {
            SPRITE_FINDER.find(centroidU, centroidV).ifPresent((sprite) -> {
                ResourceLocation spriteName = sprite.contents().name();

                MetadataRegistry.INSTANCE.metadataFromSpriteName(ModConstants.MOD_ID, spriteName)
                        .ifPresent((metadata) -> {
                            OverlayMetadata overlayMetadata = (OverlayMetadata) metadata;
                            ResourceLocation overlaySpriteName = overlayMetadata.overlaySpriteName();
                            TextureAtlasSprite overlaySprite = Minecraft.getInstance()
                                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                                    .apply(overlaySpriteName);

                            for (int vertex = 0; vertex < VERTS_PER_QUAD; vertex++) {

                                // Compute equivalent coordinates for the overlay sprite
                                float newU = OverlayQuadFunction.recomputeSpriteCoordinate(
                                        TEX_U[vertex],
                                        sprite,
                                        overlaySprite,
                                        TextureAtlasSprite::getU0,
                                        TextureAtlasSprite::getU1
                                );
                                float newV = OverlayQuadFunction.recomputeSpriteCoordinate(
                                        TEX_V[vertex],
                                        sprite,
                                        overlaySprite,
                                        TextureAtlasSprite::getV0,
                                        TextureAtlasSprite::getV1
                                );

                                int lightU;
                                int lightV;
                                int red;
                                int green;
                                int blue;
                                if (overlayMetadata.isEmissive()) {
                                    lightU = LightTexture.FULL_BRIGHT;
                                    lightV = LightTexture.FULL_BRIGHT;

                                    red = 255;
                                    green = 255;
                                    blue = 255;
                                } else {
                                    lightU = LIGHT_U[vertex];
                                    lightV = LIGHT_V[vertex];
                                    red = RED[vertex];
                                    green = GREEN[vertex];
                                    blue = BLUE[vertex];
                                }

                                TASKS[vertex].forEach((task) -> task.run(
                                        DELEGATE,
                                        newU,
                                        newV,
                                        lightU,
                                        lightV,
                                        red,
                                        green,
                                        blue
                                ));
                                DELEGATE.endVertex();

                            }
                        });
                    }
            );
        }

        currentVertex = 0;
        for (int vertex = 0; vertex < VERTS_PER_QUAD; vertex++) {
            TASKS[vertex].clear();
        }
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, r, g, b) -> delegate.defaultColor(red, green, blue, alpha));
    }

    @Override
    public void unsetDefaultColor() {
        TASKS[currentVertex].add((delegate, texU, texV, lightU, lightV, r, g, b) -> delegate.unsetDefaultColor());
    }

    /**
     * Represents a method call to this buffer that is not executed immediately.
     * @author soir20
     */
    private interface Task {

        /**
         * Runs this task with the given buffer.
         * @param buffer    buffer to run the task on
         * @param texU      u-coordinate of the texture sprite on the block atlas
         * @param texV      v-coordinate of the texture sprite on the block atlas
         * @param lightU    u-coordinate of the light texture
         * @param lightV    v-coordinate of the light texture
         * @param red       red component of the vertex's color
         * @param green     green component of the vertex's color
         * @param blue      blue component of the vertex's color
         */
        void run(VertexConsumer buffer, float texU, float texV, int lightU, int lightV, int red, int green, int blue);

    }

}
