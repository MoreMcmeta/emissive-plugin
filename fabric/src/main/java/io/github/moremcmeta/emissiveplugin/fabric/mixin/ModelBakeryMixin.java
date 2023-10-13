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

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

/**
 * Adds overlay textures to the list of sprites and wraps models with an overlay, if needed.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ModelBakery.class)
public final class ModelBakeryMixin {

    /**
     * Adds overlay textures to the list of sprites to be stitched.
     * @param materialsByAtlas      current materials to be stitched by texture atlas location
     * @return materials map with overlay sprites added
     */
    @ModifyVariable(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiling/ProfilerFiller;I)V", at = @At("STORE"), ordinal = 0)
    private Map<ResourceLocation, List<Material>> moremcmeta_emissive_addOverlaySprites(
            Map<ResourceLocation, List<Material>> materialsByAtlas
    ) {
        ModConstants.SPRITE_REGISTRAR.accept(materialsByAtlas);
        return materialsByAtlas;
    }

    /**
     * Wraps models that need to be able to render an overlay.
     * @param modelLocation     location of the model being baked
     * @param state             state of the model being baked
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_wrapModels(ResourceLocation modelLocation, ModelState state,
                                                CallbackInfoReturnable<BakedModel> callbackInfo) {
        @SuppressWarnings("DataFlowIssue")
        ModelBakery bakery = (ModelBakery) (Object) this;
        boolean usesOverlay = ModConstants.USES_OVERLAY.applyAsBoolean(
                bakery,
                bakery.getModel(modelLocation)
        );

        BakedModel original = callbackInfo.getReturnValue();

        // Built-in models are empty, and wrapping them causes shulker boxes, etc. to be invisible in the inventory
        if (usesOverlay && !(original instanceof OverlayBakedModel) && !(original instanceof BuiltInModel)
                && !(original instanceof MultiPartBakedModel) && !(original instanceof WeightedBakedModel)) {
            callbackInfo.setReturnValue(
                    new OverlayBakedModel(original)
            );
        }
    }

}
