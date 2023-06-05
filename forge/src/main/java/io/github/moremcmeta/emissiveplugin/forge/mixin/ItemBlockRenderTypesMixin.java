package io.github.moremcmeta.emissiveplugin.forge.mixin;

import io.github.moremcmeta.emissiveplugin.forge.render.EmissiveModelBlockRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Allows blocks to be rendered in the {@link RenderType#translucent()} render layer if an overlay is
 * being rendered on Forge.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {

    /**
     * Allows blocks to be rendered in the {@link RenderType#translucent()} render layer if an overlay is
     * being rendered on Forge.
     * @param state         state of the block being rendered
     * @param type          layer that is being rendered
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "canRenderInLayer(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/RenderType;)Z",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void moremcmeta_emissive_enableBlockTransparencyOverlay(BlockState state, RenderType type,
                                                                           CallbackInfoReturnable<Boolean> callbackInfo) {
        if (EmissiveModelBlockRenderer.ALWAYS_RENDER_ON_TRANSPARENCY.get() && type == RenderType.translucent()) {
            callbackInfo.setReturnValue(true);
        }
    }

}
