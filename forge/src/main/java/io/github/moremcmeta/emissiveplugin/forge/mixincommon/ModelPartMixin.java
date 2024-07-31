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

package io.github.moremcmeta.emissiveplugin.forge.mixincommon;

import io.github.moremcmeta.emissiveplugin.render.EntityRenderingState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Adjusts the light value for emissive overlays.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = ModelPart.class, priority = Integer.MIN_VALUE, remap = false)
public final class ModelPartMixin {

    /**
     * Adjusts the light value before an emissive overlay is rendered.
     * @param packedLight       original light value
     * @return new light value
     */
    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At(value = "HEAD"), ordinal = 0)
    private int moremcmeta_emissive_onRender(int packedLight) {

        // Some model renderers overwrite the light value, so additionally set it from the model part
        if (EntityRenderingState.isEmissive.get()) {
            return LightTexture.FULL_BRIGHT;
        }

        return packedLight;
    }

}