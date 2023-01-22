package io.github.moremcmeta.emissiveplugin.forge.mixin;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @ModifyVariable(method = "processLoading(Lnet/minecraft/util/profiling/ProfilerFiller;I)V",
            at = @At("STORE"), ordinal = 0, remap = false)
    private Map<ResourceLocation, List<Material>> addOverlaySprites(
            Map<ResourceLocation, List<Material>> materialsByAtlas
    ) {
        ModConstants.SPRITE_REGISTRAR.accept(materialsByAtlas);
        return materialsByAtlas;
    }

}
