/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2024 soir20
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

package io.github.moremcmeta.emissiveplugin.forge.mixincommon;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.render.EmptyVertexConsumer;
import io.github.moremcmeta.emissiveplugin.render.EntityRenderingState;
import io.github.moremcmeta.emissiveplugin.render.WrappedBufferSource;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Prepares a render buffer for sprite overlays.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = TextureAtlasSprite.class, priority = Integer.MAX_VALUE, remap = false)
public class TextureAtlasSpriteMixin {

    /**
     * Prepares a render buffer for sprite overlays.
     * @param inputVertexConsumer   original render buffer
     * @param callbackInfo          callback info from Mixin
     */
    @Inject(method = "wrap(Lcom/mojang/blaze3d/vertex/VertexConsumer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", 
            at = @At(value = "RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_onWrapSprite(VertexConsumer inputVertexConsumer, CallbackInfoReturnable<VertexConsumer> callbackInfo) {

        // An EmptyVertexConsumer means this mod supplied the buffer, so an overlay is being rendered
        if (inputVertexConsumer instanceof EmptyVertexConsumer) {
            VertexConsumer returnVertexConsumer = callbackInfo.getReturnValue();
            if (returnVertexConsumer instanceof SpriteCoordinateExpander spriteVertexConsumer) {
                ResourceLocation location = spriteVertexConsumer.sprite.contents().name();
                MetadataRegistry.INSTANCE.metadataFromSpriteName(ModConstants.MOD_ID, location)
                        .ifPresent((metadata) -> {
                            MultiBufferSource bufferSource = EntityRenderingState.currentBufferSource.get();
                            if (bufferSource instanceof WrappedBufferSource wrappedBufferSource) {
                                callbackInfo.setReturnValue(
                                        wrappedBufferSource.bufferFromMetadata((OverlayMetadata) metadata)
                                );
                            }
                        });
            }
        }

    }

}
