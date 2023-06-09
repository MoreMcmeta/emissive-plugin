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

package io.github.moremcmeta.emissiveplugin.fabric.mixin;

import io.github.moremcmeta.emissiveplugin.model.OverlayBakedQuad;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Sets a quad to full-bright if it is an emissive overlay quad.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ModelBlockRenderer.class)
public final class ModelBlockRendererMixin {

    /**
     * Sets a quad to full-bright if it is an emissive overlay quad.
     * @param args      all method arguments from Mixin
     */
    @ModifyArgs(method = "renderQuadList", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFII)V"))
    private static void moremcmeta_emissive_onPutBulkData(Args args) {
        BakedQuad quad = args.get(1);
        if (quad instanceof OverlayBakedQuad overlayQuad && overlayQuad.isEmissive()) {
            args.set(5, LightTexture.FULL_BRIGHT);
        }
    }

}
