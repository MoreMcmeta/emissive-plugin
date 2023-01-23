package io.github.moremcmeta.emissiveplugin.fabric.mixin;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayBakedModel;
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

@SuppressWarnings("unused")
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @ModifyVariable(method = "<init>(Lnet/minecraft/server/packs/resources;Lnet/minecraft/client/color/block;Lnet/minecraft/util/profiling/ProfilerFiller;I)V", at = @At("STORE"), ordinal = 0)
    private Map<ResourceLocation, List<Material>> addOverlaySprites(
            Map<ResourceLocation, List<Material>> materialsByAtlas
    ) {
        ModConstants.SPRITE_REGISTRAR.accept(materialsByAtlas);
        return materialsByAtlas;
    }

    @Inject(method = "bake", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void wrapModels(ResourceLocation modelLocation, ModelState state,
                            CallbackInfoReturnable<BakedModel> callbackInfo) {
        @SuppressWarnings("DataFlowIssue")
        ModelBakery bakery = (ModelBakery) (Object) this;
        boolean usesOverlay = ModConstants.USES_OVERLAY.applyAsBoolean(
                bakery,
                bakery.getModel(modelLocation)
        );

        BakedModel original = callbackInfo.getReturnValue();
        if (usesOverlay && !(original instanceof OverlayBakedModel)) {
            callbackInfo.setReturnValue(
                    new OverlayBakedModel(original)
            );
        }
    }

}
