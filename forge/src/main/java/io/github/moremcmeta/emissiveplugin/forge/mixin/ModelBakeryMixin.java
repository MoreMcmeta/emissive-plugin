package io.github.moremcmeta.emissiveplugin.forge.mixin;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.forge.model.OverlayBakedItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Adds overlay textures to the list of sprites and wraps models with an overlay, if needed.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    /**
     * Adds overlay textures to the list of sprites to be stitched.
     * @param materialsByAtlas      current materials to be stitched by texture atlas location
     * @return materials map with overlay sprites added
     */
    @ModifyVariable(method = "processLoading(Lnet/minecraft/util/profiling/ProfilerFiller;I)V",
            at = @At("STORE"), ordinal = 0, remap = false)
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
     * @param materialToSprite  retrieves an atlas sprite given a material
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;Ljava/util/function/Function;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private void moremcmeta_emissive_wrapModels(ResourceLocation modelLocation, ModelState state,
                                                Function<Material, TextureAtlasSprite> materialToSprite,
                                                CallbackInfoReturnable<BakedModel> callbackInfo) {
        @SuppressWarnings("DataFlowIssue")
        ModelBakery bakery = (ModelBakery) (Object) this;
        boolean usesOverlay = ModConstants.USES_OVERLAY.applyAsBoolean(
                bakery,
                bakery.getModel(modelLocation)
        );

        BakedModel original = callbackInfo.getReturnValue();
        if (usesOverlay && !(original instanceof OverlayBakedItemModel)) {
            callbackInfo.setReturnValue(
                    new OverlayBakedItemModel(callbackInfo.getReturnValue())
            );
        }
    }

}
