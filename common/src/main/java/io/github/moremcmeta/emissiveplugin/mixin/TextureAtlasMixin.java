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

package io.github.moremcmeta.emissiveplugin.mixin;

import io.github.moremcmeta.emissiveplugin.fabricapi.SpriteFinder;
import io.github.moremcmeta.emissiveplugin.mixinaccess.SpriteFinderSupplier;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Updates the {@link SpriteFinder} when the texture atlas reloads.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(TextureAtlas.class)
public final class TextureAtlasMixin implements SpriteFinderSupplier {
    @Unique
    private SpriteFinder spriteFinder;

    /**
     * Updates the sprite finder when the texture atlas reloads sprites.
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "reload(Lnet/minecraft/client/renderer/texture/TextureAtlas$Preparations;)V", at = @At(value = "RETURN"))
    public void moremcmeta_emissive_onReload(CallbackInfo callbackInfo) {
        spriteFinder = new SpriteFinder((TextureAtlas) (Object) this);
    }

    @Unique
    @Override
    public SpriteFinder moremcmeta_emissive_spriteFinder() {
        return spriteFinder;
    }
}
