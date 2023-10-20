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

package io.github.moremcmeta.emissiveplugin.forge.mixin;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.forge.model.OverlayBakedItemModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps all models with an overlay. The wrapper checks if an overlay is needed when quads are
 * retrieved; wrapping all models does not give them all an overlay.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ModelBakery.ModelBakerImpl.class)
public final class ModelBakeryMixin {
    @Unique
    private ModelBakery bakery;

    /**
     * Wraps models that need to be able to render an overlay.
     * @param modelLocation     location of the model being baked
     * @param state             state of the model being baked
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_wrapModels(ResourceLocation modelLocation, ModelState state,
                                                CallbackInfoReturnable<BakedModel> callbackInfo) {
        ModelBakery.ModelBakerImpl bakeryImpl = (ModelBakery.ModelBakerImpl) (Object) this;
        UnbakedModel unbakedModel = bakeryImpl.getModel(modelLocation);
        boolean usesOverlay = moremcmeta_emissive_usesOverlay(bakeryImpl, unbakedModel);

        BakedModel original = callbackInfo.getReturnValue();
        BakedModel resultModel = original;

        // Built-in models are empty, and wrapping them causes shulker boxes, etc. to be invisible in the inventory
        if (usesOverlay && !(original instanceof OverlayBakedItemModel) && !(original instanceof BuiltInModel)) {
            resultModel = new OverlayBakedItemModel(original);
            callbackInfo.setReturnValue(resultModel);
        }

        ModelBakery.BakedCacheKey key = new ModelBakery.BakedCacheKey(modelLocation, state.getRotation(), state.isUvLocked());
        if (bakery != null && bakery.bakedCache.containsKey(key)) {
            bakery.bakedCache.put(key, resultModel);
        }
    }

    /**
     * Checks whether a model needs to be wrapped with an overlay model.
     * @param bakeryImpl    model bakery
     * @param model         model to check
     * @return whether the model needs to be wrapped with an overlay model
     */
    @Unique
    private boolean moremcmeta_emissive_usesOverlay(ModelBakery.ModelBakerImpl bakeryImpl, UnbakedModel model) {
        boolean usesOverlay = true;

        // Filter out block models for which we can check materials to improve performance
        if (model instanceof BlockModel blockModel) {
            Set<Material> materials = moremcmeta_emissive_modelMaterials(blockModel).stream()
                    .map(blockModel::getMaterial)
                    .collect(Collectors.toSet());
            usesOverlay = ModConstants.USES_OVERLAY.test(materials);
        } else if (model instanceof MultiPart multiPartModel) {
            usesOverlay = multiPartModel.getMultiVariants().stream()
                    .anyMatch((part) -> moremcmeta_emissive_usesOverlay(bakeryImpl, part));
        } else if (model instanceof MultiVariant multiVariantModel) {
            usesOverlay = multiVariantModel.getVariants().stream()
                    .anyMatch((variant) -> moremcmeta_emissive_usesOverlay(
                            bakeryImpl,
                            bakeryImpl.getModel(variant.getModelLocation())
                    ));
        }

        return usesOverlay;
    }

    /**
     * Gets all materials for a given model.
     * @param model     model to retrieve materials for
     * @return all materials in this model and its parent models
     */
    private Set<String> moremcmeta_emissive_modelMaterials(BlockModel model) {
        Set<String> materials = new HashSet<>();

        BlockModel currentModel = model;
        while (currentModel != null) {
            materials.addAll(currentModel.textureMap.keySet());
            currentModel = currentModel.parent;
        }

        return materials;
    }

}
