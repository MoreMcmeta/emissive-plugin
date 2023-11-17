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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Contains factories for custom emissive overlay {@link RenderType}s.
 * @author soir20
 */
public final class CustomRenderTypes extends RenderStateShard {

    /**
     * Creates a new translucent entity render type with z-layering enabled, which works with armor.
     * @param textureLocation       location of the texture to render
     * @return translucent render type
     */
    public static RenderType entityTranslucentZLayering(ResourceLocation textureLocation) {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(textureLocation, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDiffuseLightingState(DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);
        return new RenderType.CompositeRenderType(
                "entity_translucent_z_layering",
                DefaultVertexFormat.NEW_ENTITY,
                7,
                256,
                true,
                true,
                compositeState
        );
    }

    /**
     * Prevents this class from being constructed.
     * @param name          name of the render type
     * @param setupState    sets up rendering for this render type
     * @param clearState    cleans up rendering for this render type
     */
    private CustomRenderTypes(String name, Runnable setupState, Runnable clearState) {
        super(name, setupState, clearState);
    }

}
