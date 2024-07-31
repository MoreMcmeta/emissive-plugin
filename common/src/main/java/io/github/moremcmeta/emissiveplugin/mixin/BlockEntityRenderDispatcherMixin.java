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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Sets the current {@link EntityRenderingState} when block entities are rendered. Priority is set
 *  * so that this Mixin runs after all other Mixins (particularly Iris) for compatibility.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = BlockEntityRenderDispatcher.class, priority = Integer.MAX_VALUE)
public final class BlockEntityRenderDispatcherMixin {

    /**
     * Renders overlays for block entities.
     * @param blockEntityRenderer   renderer for the given block entity
     * @param blockEntity           block entity being rendered
     * @param tickDelta             ticks since the last render
     * @param poseStack             pose stack
     * @param bufferSource          source of render buffers
     * @param callbackInfo          callback info from Mixin
     * @param packedLight           packed coordinates for the light texture
     */
    @Inject(method = "setupAndRender(Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void moremcmeta_emissive_onRender(BlockEntityRenderer<BlockEntity> blockEntityRenderer,
                                                     BlockEntity blockEntity, float tickDelta, PoseStack poseStack,
                                                     MultiBufferSource bufferSource, CallbackInfo callbackInfo, int packedLight) {
        moremcmeta_emissive_renderBlockEntityOverlay(
                blockEntityRenderer,
                blockEntity,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                tickDelta
        );
    }

    /**
     * Renders overlays for block entity items.
     * @param blockEntity           block entity being rendered
     * @param poseStack             pose stack
     * @param bufferSource          source of render buffers
     * @param packedLight           packed coordinates for the light texture
     * @param packedOverlay         packed coordinates for the overlay texture
     * @param callbackInfo          callback info from Mixin
     * @param blockEntityRenderer   renderer for the given block entity
     */
    @Inject(method = "renderItem", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_onBlockEntityItemRender(BlockEntity blockEntity, PoseStack poseStack,
                                                             MultiBufferSource bufferSource, int packedLight,
                                                             int packedOverlay, CallbackInfoReturnable<Boolean> callbackInfo,
                                                             BlockEntityRenderer<BlockEntity> blockEntityRenderer) {
        moremcmeta_emissive_renderBlockEntityOverlay(
                blockEntityRenderer,
                blockEntity,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
                0.0f
        );
    }

    /**
     * Renders the overlay for a block entity or block entity item.
     * @param blockEntityRenderer   renderer for the given block entity
     * @param blockEntity           block entity being rendered
     * @param poseStack             pose stack
     * @param bufferSource          source of render buffers
     * @param packedLight           packed coordinates for the light texture
     * @param packedOverlay         packed coordinates for the overlay texture
     * @param tickDelta             ticks since the last render
     */
    @Unique
    private static void moremcmeta_emissive_renderBlockEntityOverlay(BlockEntityRenderer<BlockEntity> blockEntityRenderer,
                                                                     BlockEntity blockEntity, PoseStack poseStack,
                                                                     MultiBufferSource bufferSource,  int packedLight,
                                                                     int packedOverlay, float tickDelta) {
        if (bufferSource instanceof WrappedBufferSource) {
            return;
        }

        if (blockEntityRenderer == null) {
            return;
        }

        blockEntityRenderer.render(
                blockEntity,
                tickDelta,
                poseStack,
                WrappedBufferSource.wrap(bufferSource, false, true),
                packedLight,
                packedOverlay
        );
        EntityRenderingState.isEmissive.set(true);
        blockEntityRenderer.render(
                blockEntity,
                tickDelta,
                poseStack,
                WrappedBufferSource.wrap(bufferSource, true, true),
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );
        EntityRenderingState.isEmissive.set(false);
        EntityRenderingState.currentBufferSource.remove();
    }

}
