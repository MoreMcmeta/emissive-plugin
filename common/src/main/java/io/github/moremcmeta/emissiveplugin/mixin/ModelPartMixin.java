/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.WrappedBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ModelPart.class)
public class ModelPartMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD"))
    private void onEntry(CallbackInfo callbackInfo) {
        WrappedBufferSource.depth++;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onReturn(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float blue, float green, float alpha, CallbackInfo callbackInfo) {
        ModelPart thisPart = (ModelPart) (Object) this;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        if (WrappedBufferSource.depth == 0 && WrappedBufferSource.currentRenderType instanceof RenderType.CompositeRenderType compositeType) {
            RenderType.CompositeState state = compositeType.state();

            ResourceLocation location = null;
            if (vertexConsumer instanceof SpriteCoordinateExpander spriteVertexConsumer) {
                location = spriteVertexConsumer.sprite.getName();
            } else if (state.textureState.cutoutTexture().isPresent()) {
                location = state.textureState.cutoutTexture().get();
            }

            if (location != null && location.getPath().contains("bed")) {
                RenderType lastType = WrappedBufferSource.currentRenderType;
                VertexConsumer newConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCullZOffset(new ResourceLocation("textures/entity/bed/blue.png")));
                thisPart.render(poseStack, newConsumer, LightTexture.FULL_BRIGHT, packedOverlay, red, blue, green, alpha);

                // Restore original render type
                bufferSource.getBuffer(lastType);

            }
        }

        WrappedBufferSource.depth--;
    }
}
