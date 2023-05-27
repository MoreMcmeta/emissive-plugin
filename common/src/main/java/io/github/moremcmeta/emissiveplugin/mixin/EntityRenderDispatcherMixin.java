/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.moremcmeta.emissiveplugin.mixin;

import io.github.moremcmeta.emissiveplugin.render.EntityRenderingState;
import io.github.moremcmeta.emissiveplugin.render.WrappedBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"))
    private MultiBufferSource moremcmeta_emissive_wrapBufferSource(MultiBufferSource bufferSource) {
        return WrappedBufferSource.wrap(bufferSource, EntityRenderingState.currentRenderType::set);
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void moremcmeta_emissive_onReturn(CallbackInfo callbackInfo) {
        EntityRenderingState.currentRenderType.set(null);
    }

}
