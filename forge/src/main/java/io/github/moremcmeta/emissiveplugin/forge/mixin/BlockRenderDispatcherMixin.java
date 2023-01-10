package io.github.moremcmeta.emissiveplugin.forge.mixin;

import io.github.moremcmeta.emissiveplugin.forge.render.EmissiveModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {
    @Redirect(method = "<init>(Lnet/minecraft/client/renderer/block/BlockModelShaper;Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;Lnet/minecraft/client/color/block/BlockColors;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;modelRenderer:Lnet/minecraft/client/renderer/block/ModelBlockRenderer;", opcode = Opcodes.PUTFIELD))
    private void replaceBlockRenderer(BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer) {
        dispatcher.modelRenderer = new EmissiveModelBlockRenderer(dispatcher.blockColors);
    }
}
