package io.github.moremcmeta.emissiveplugin.fabric.mixin;

import io.github.moremcmeta.emissiveplugin.fabric.OverlayBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            method = "apply")
    @SuppressWarnings("ConstantConditions")
    public void onReloaded(CallbackInfo info) {
        Map<ResourceLocation, BakedModel> models = ((ModelManager) (Object) this).bakedRegistry;
        models.forEach(((location, bakedModel) -> models.put(location, new OverlayBakedModel(bakedModel))));
    }

}
