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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.EntityRenderingState;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ParsedMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Function;

@Mixin(ModelPart.class)
public class ModelPartMixin {
    private final Minecraft MINECRAFT = Minecraft.getInstance();
    private ResourceLocation lastAtlas;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At(value = "HEAD"))
    private void onEntry(CallbackInfo callbackInfo) {
        EntityRenderingState.partRenderDepth++;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onReturn(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                          float red, float blue, float green, float alpha, CallbackInfo callbackInfo) {
        ModelPart thisPart = (ModelPart) (Object) this;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Check depth to avoid getting stuck in infinite recursion or re-rendering child parts multiple times
        if (EntityRenderingState.partRenderDepth == 0) {
            Optional<ParsedMetadata> metadataOptional = Optional.empty();

            // Handle a sprite being rendered
            if (vertexConsumer instanceof SpriteCoordinateExpander spriteVertexConsumer) {
                ResourceLocation location = spriteVertexConsumer.sprite.getName();
                metadataOptional = MetadataRegistry.INSTANCE.metadataFromSpriteName(ModConstants.DISPLAY_NAME, location);

            // Handle a regular texture being rendered
            } else if (EntityRenderingState.currentRenderType instanceof RenderType.CompositeRenderType compositeType
                    && compositeType.state().textureState.cutoutTexture().isPresent()) {

                ResourceLocation location = compositeType.state().textureState.cutoutTexture().get();
                metadataOptional = MetadataRegistry.INSTANCE.metadataFromPath(ModConstants.DISPLAY_NAME, location);
            }

            // Do rendering
            if (metadataOptional.isPresent()) {
                OverlayMetadata overlayMetadata = (OverlayMetadata) metadataOptional.get();
                ResourceLocation overlay = overlayMetadata.overlayLocation();
                int overlayLight = overlayMetadata.isEmissive() ? LightTexture.FULL_BRIGHT : packedLight;

                RenderType lastType = EntityRenderingState.currentRenderType;
                VertexConsumer newConsumer = makeBuffer(bufferSource, overlay, RenderType::entityCutoutNoCullZOffset);
                thisPart.render(poseStack, newConsumer, overlayLight, packedOverlay, red, blue, green, alpha);

                // Restore original render type
                bufferSource.getBuffer(lastType);

            }

        }

        EntityRenderingState.partRenderDepth--;
    }

    private VertexConsumer makeBuffer(MultiBufferSource bufferSource, ResourceLocation overlayLocation,
                                      Function<ResourceLocation, RenderType> renderTypeFunction) {
        ResourceLocation spriteName = makeSpriteName(overlayLocation);
        Optional<VertexConsumer> spriteBuffer;

        // Check the cached atlas before checking all atlases
        if (lastAtlas != null) {
            spriteBuffer = makeBufferIfSprite(
                    lastAtlas,
                    overlayLocation,
                    spriteName,
                    bufferSource,
                    renderTypeFunction
            );

            if (spriteBuffer.isPresent()) {
                return spriteBuffer.get();
            }
        }

        // Check all atlases if the texture is no longer in that atlas
        for (ResourceLocation atlasLocation : ModConstants.ATLAS_LOCATIONS) {
            spriteBuffer = makeBufferIfSprite(
                    atlasLocation,
                    overlayLocation,
                    spriteName,
                    bufferSource,
                    renderTypeFunction
            );

            if (spriteBuffer.isPresent()) {
                lastAtlas = atlasLocation;
                return spriteBuffer.get();
            }
        }

        // Texture isn't a sprite
        return bufferSource.getBuffer(renderTypeFunction.apply(overlayLocation));

    }

    private Optional<VertexConsumer> makeBufferIfSprite(ResourceLocation atlasLocation,
                                                        ResourceLocation overlayLocation,
                                                        ResourceLocation spriteName,
                                                        MultiBufferSource bufferSource,
                                                        Function<ResourceLocation, RenderType> renderTypeFunction) {
        AbstractTexture abstractTexture = MINECRAFT.getTextureManager().getTexture(atlasLocation);
        if (!(abstractTexture instanceof TextureAtlas atlas)) {
            LogManager.getLogger().warn(
                    "Atlas {} is not a subclass of TextureAtlas; sprites from this atlas will not be used as overlays",
                    atlasLocation
            );
            return Optional.empty();
        }

        TextureAtlasSprite sprite = atlas.getSprite(spriteName);
        if (sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
            sprite = atlas.getSprite(overlayLocation);
        }

        if (!sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
            RenderType renderType = renderTypeFunction.apply(atlasLocation);
            return Optional.of(sprite.wrap(bufferSource.getBuffer(renderType)));
        }

        return Optional.empty();
    }

    private ResourceLocation makeSpriteName(ResourceLocation textureLocation) {
        int prefixLength = "textures/".length();
        int postfixLength = ".png".length();
        int pathLength = textureLocation.getPath().length();

        return new ResourceLocation(
                textureLocation.getNamespace(),
                textureLocation.getPath().substring(prefixLength, pathLength - postfixLength)
        );
    }
}
