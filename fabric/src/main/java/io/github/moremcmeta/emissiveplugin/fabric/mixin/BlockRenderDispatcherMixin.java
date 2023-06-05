package io.github.moremcmeta.emissiveplugin.fabric.mixin;

import io.github.moremcmeta.emissiveplugin.fabric.render.EmissiveModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces Minecraft's default {@link ModelBlockRenderer} with one that supports emissive overlays.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {

    /**
     * Replaces Minecraft's default {@link ModelBlockRenderer} with one that supports emissive overlays.
     * @param dispatcher    Minecraft's default {@link BlockRenderDispatcher}
     * @param renderer      Minecraft's default {@link ModelBlockRenderer}
     */
    @Redirect(method = "<init>(Lnet/minecraft/client/renderer/block/BlockModelShaper;Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;Lnet/minecraft/client/color/block/BlockColors;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;modelRenderer:Lnet/minecraft/client/renderer/block/ModelBlockRenderer;", opcode = Opcodes.PUTFIELD))
    private void moremcmeta_emissive_replaceBlockRenderer(BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer) {
        dispatcher.modelRenderer = new EmissiveModelBlockRenderer(dispatcher.blockColors);
    }

}