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
import io.github.moremcmeta.emissiveplugin.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.WrappedBufferSource;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ParsedMetadata;
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

import java.util.Optional;

@Mixin(ModelPart.class)
public class ModelPartMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At(value = "HEAD"))
    private void onEntry(CallbackInfo callbackInfo) {
        WrappedBufferSource.depth++;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onReturn(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                          float red, float blue, float green, float alpha, CallbackInfo callbackInfo) {
        ModelPart thisPart = (ModelPart) (Object) this;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        if (WrappedBufferSource.depth == 0) {
            Optional<ParsedMetadata> metadataOptional = Optional.empty();
            if (vertexConsumer instanceof SpriteCoordinateExpander spriteVertexConsumer) {
                ResourceLocation location = spriteVertexConsumer.sprite.getName();
                metadataOptional = MetadataRegistry.INSTANCE.metadataFromSpriteName(ModConstants.DISPLAY_NAME, location);
            } else if (WrappedBufferSource.currentRenderType instanceof RenderType.CompositeRenderType compositeType && compositeType.state().textureState.cutoutTexture().isPresent()) {
                ResourceLocation location = compositeType.state().textureState.cutoutTexture().get();
                metadataOptional = MetadataRegistry.INSTANCE.metadataFromPath(ModConstants.DISPLAY_NAME, location);
            }

            if (metadataOptional.isPresent()) {
                OverlayMetadata overlayMetadata = (OverlayMetadata) metadataOptional.get();
                ResourceLocation overlay = overlayMetadata.overlayLocation();
                int overlayLight = overlayMetadata.isEmissive() ? LightTexture.FULL_BRIGHT : packedLight;

                RenderType lastType = WrappedBufferSource.currentRenderType;
                VertexConsumer newConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCullZOffset(overlay));
                thisPart.render(poseStack, newConsumer, overlayLight, packedOverlay, red, blue, green, alpha);

                // Restore original render type
                bufferSource.getBuffer(lastType);

            }
        }

        WrappedBufferSource.depth--;
    }
}
