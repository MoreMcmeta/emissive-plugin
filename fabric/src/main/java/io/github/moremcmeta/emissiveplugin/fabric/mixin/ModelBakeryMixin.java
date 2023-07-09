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

import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Wraps all models with an overlay. The wrapper checks if an overlay is needed when quads are
 * retrieved; wrapping all models does not give them all an overlay.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ModelBakery.ModelBakerImpl.class)
public final class ModelBakeryMixin {

    /**
     * Wraps models that need to be able to render an overlay.
     * @param modelLocation     location of the model being baked
     * @param state             state of the model being baked
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_wrapModels(ResourceLocation modelLocation, ModelState state,
                                                CallbackInfoReturnable<BakedModel> callbackInfo) {
        BakedModel original = callbackInfo.getReturnValue();

        // Built-in models are empty, and wrapping them causes shulker boxes, etc. to be invisible in the inventory
        if (!(original instanceof OverlayBakedModel) && !(original instanceof BuiltInModel)) {
            callbackInfo.setReturnValue(
                    new OverlayBakedModel(original)
            );
        }
    }

}
