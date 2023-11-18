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

import com.mojang.blaze3d.platform.NativeImage;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adds all overlays to the block texture atlas for models that require stitched sprites.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(SpriteLoader.class)
public final class SpriteLoaderMixin {
    @Shadow
    private ResourceLocation location;

    /**
     * Adds overlay textures to the list of sprites to be stitched.
     * @param originalSprites      current sprites to be stitched
     * @return list with all original sprites and the newly-added sprites
     */
    @ModifyVariable(
            method = "stitch(Ljava/util/List;ILjava/util/concurrent/Executor;)Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;",
            at = @At("HEAD"),
            ordinal = 0
    )
    private List<SpriteContents> moremcmeta_emissive_addOverlaySprites(
            List<SpriteContents> originalSprites
    ) {
        if (!location.equals(TextureAtlas.LOCATION_BLOCKS)) {
            return originalSprites;
        }

        List<SpriteContents> sprites = new ArrayList<>(originalSprites);
        Set<ResourceLocation> spriteTextures = sprites.stream().map(SpriteContents::name).collect(Collectors.toSet());

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        MetadataRegistry.INSTANCE.metadataByPlugin(ModConstants.MOD_ID).forEach(
                (textureLocation, metadata) -> {
                    OverlayMetadata overlayMetadata = (OverlayMetadata) metadata;
                    if (spriteTextures.contains(overlayMetadata.overlaySpriteName())) {
                        return;
                    }

                    ResourceLocation overlayTexturePath = SpriteName.toTexturePath(overlayMetadata.overlaySpriteName());
                    Optional<Resource> resource = resourceManager.getResource(
                            overlayTexturePath
                    );

                    resource.ifPresent((rsc) -> {
                        try {
                            AnimationMetadataSection animationMetadata = rsc.metadata()
                                    .getSection(AnimationMetadataSection.SERIALIZER)
                                    .orElse(AnimationMetadataSection.EMPTY);
                            NativeImage spriteImage = NativeImage.read(rsc.open());

                            FrameSize frameSize = animationMetadata.calculateFrameSize(
                                    spriteImage.getWidth(),
                                    spriteImage.getHeight()
                            );

                            sprites.add(
                                    new SpriteContents(
                                            overlayMetadata.overlaySpriteName(),
                                            frameSize,
                                            spriteImage,
                                            rsc.metadata()
                                    )
                            );
                        } catch (IOException err) {
                            LogManager.getLogger().error(
                                    "Unable to read texture {} while stitching it to the block atlas: {}",
                                    overlayTexturePath,
                                    err
                            );
                        }
                    });
                }
        );

        return sprites;
    }

}
