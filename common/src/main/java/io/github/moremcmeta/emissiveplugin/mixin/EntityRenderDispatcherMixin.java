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

package io.github.moremcmeta.emissiveplugin.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.moremcmeta.emissiveplugin.render.EntityRenderingState;
import io.github.moremcmeta.emissiveplugin.render.WrappedBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Sets the current {@link EntityRenderingState} when regular entities are rendered. Priority is set
 * so that this Mixin runs after all other Mixins (particularly Iris) for compatibility.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = EntityRenderDispatcher.class, priority = Integer.MAX_VALUE)
public final class EntityRenderDispatcherMixin {

    /**
     * Renders overlays for non-block entities.
     * @param entity            entity being rendered
     * @param x                 x-coordinate of the entity
     * @param y                 y-coordinate of the entity
     * @param z                 z-coordinate of the entity
     * @param yaw               yaw of the entity
     * @param tickDelta         ticks since the entity was last rendered
     * @param poseStack         pose stack
     * @param bufferSource      source of render buffers
     * @param packedLight       packed coordinates for the light texture
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "render", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_onRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
                                              PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                              CallbackInfo callbackInfo) {
        if (bufferSource instanceof WrappedBufferSource || EntityRenderingState.currentBufferSource.get() != null) {
            return;
        }

        @SuppressWarnings("DataFlowIssue")
        EntityRenderDispatcher entityRenderDispatcher = ((EntityRenderDispatcher) (Object) this);
        entityRenderDispatcher.render(
                entity,
                x,
                y,
                z,
                yaw,
                tickDelta,
                poseStack,
                WrappedBufferSource.wrap(bufferSource, false, false),
                packedLight
        );
        EntityRenderingState.isEmissive.set(true);
        entityRenderDispatcher.render(
                entity,
                x,
                y,
                z,
                yaw,
                tickDelta,
                poseStack,
                WrappedBufferSource.wrap(bufferSource, true, false),
                LightTexture.FULL_BRIGHT
        );
        EntityRenderingState.isEmissive.set(false);
        EntityRenderingState.currentBufferSource.remove();
    }

}
