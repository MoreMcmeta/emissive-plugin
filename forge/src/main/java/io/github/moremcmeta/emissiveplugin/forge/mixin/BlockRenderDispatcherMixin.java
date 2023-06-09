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

import io.github.moremcmeta.emissiveplugin.forge.render.EmissiveModelBlockRenderer;
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
