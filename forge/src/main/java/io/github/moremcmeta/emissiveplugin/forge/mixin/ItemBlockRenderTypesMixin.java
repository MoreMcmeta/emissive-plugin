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
public final class ItemBlockRenderTypesMixin {

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
