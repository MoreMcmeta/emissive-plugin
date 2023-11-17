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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.render.CustomRenderTypes;
import io.github.moremcmeta.emissiveplugin.render.EntityRenderingState;
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Function;

/**
 * Renders overlays over {@link ModelPart}s.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ModelPart.class)
public final class ModelPartMixin {
    private final Minecraft MINECRAFT = Minecraft.getInstance();

    /**
     * Sets the render depth to track when the parent model part has finished rendering.
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At(value = "HEAD"))
    private void moremcmeta_emissive_onEntry(CallbackInfo callbackInfo) {
        EntityRenderingState.partRenderDepth.set(EntityRenderingState.partRenderDepth.get() + 1);
    }

    /**
     * Renders overlay quads once the base model part and its children have finished rendering.
     * @param poseStack         pose stack for rendering
     * @param vertexConsumer    render buffer
     * @param packedLight       light strength packed into an integer
     * @param packedOverlay     overlay coordinates packed into an integer
     * @param red               red component of light
     * @param green              blue component of light
     * @param blue             green component of light
     * @param alpha             alpha component of light
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_emissive_onReturn(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                                              int packedOverlay, float red, float green, float blue, float alpha,
                                              CallbackInfo callbackInfo) {

        @SuppressWarnings("DataFlowIssue")
        ModelPart thisPart = (ModelPart) (Object) this;

        // Check depth to avoid getting stuck in infinite recursion or re-rendering child parts multiple times
        if (EntityRenderingState.partRenderDepth.get() == 0) {
            Optional<AnalyzedMetadata> metadataOptional = Optional.empty();

            // Handle a sprite being rendered
            if (vertexConsumer instanceof SpriteCoordinateExpander spriteVertexConsumer) {
                ResourceLocation location = spriteVertexConsumer.sprite.getName();
                metadataOptional = MetadataRegistry.INSTANCE.metadataFromSpriteName(ModConstants.MOD_ID, location);

            // Handle a regular texture being rendered
            } else if (EntityRenderingState.currentRenderType.get() instanceof RenderType.CompositeRenderType compositeType
                    && compositeType.state().textureState.cutoutTexture().isPresent()) {

                ResourceLocation location = compositeType.state().textureState.cutoutTexture().get();
                metadataOptional = MetadataRegistry.INSTANCE.metadataFromPath(ModConstants.MOD_ID, location);
            }

            // Do rendering
            MultiBufferSource bufferSource = EntityRenderingState.currentBufferSource.get();
            RenderType lastType = EntityRenderingState.currentRenderType.get();
            if (metadataOptional.isPresent() && bufferSource != null && lastType != null) {
                OverlayMetadata overlayMetadata = (OverlayMetadata) metadataOptional.get();
                ResourceLocation overlay = overlayMetadata.overlaySpriteName();
                int overlayLight = overlayMetadata.isEmissive() ? LightTexture.FULL_BRIGHT : packedLight;


                /* Disabling cull is needed to render emissive layers inside the slime properly, but it needs to
                   be enabled for bed overlays to render properly. Z-layering needs to be enabled for armor overlays
                   to render properly, so the entity shadow type is used. */
                Function<ResourceLocation, RenderType> overlayType = EntityRenderingState.isBlockEntity.get()
                        ? RenderType::entityTranslucentCull
                        : CustomRenderTypes::entityTranslucentZLayering;

                VertexConsumer newConsumer = makeBuffer(bufferSource, overlay, overlayType);
                thisPart.render(poseStack, newConsumer, overlayLight, packedOverlay, red, green, blue, alpha);

                // Restore original render type
                bufferSource.getBuffer(lastType);

            }

        }

        EntityRenderingState.partRenderDepth.set(EntityRenderingState.partRenderDepth.get() - 1);
    }

    /**
     * Creates a buffer to render an overlay texture.
     * @param bufferSource          source of buffers for rendering
     * @param spriteName            name of the overlay texture as a sprite
     * @param renderTypeFunction    creates a render type given the location of a texture used while rendering
     * @return buffer to render the overlay texture
     */
    @Unique
    private VertexConsumer makeBuffer(MultiBufferSource bufferSource, ResourceLocation spriteName,
                                      Function<ResourceLocation, RenderType> renderTypeFunction) {
        ResourceLocation overlayLocation = SpriteName.toTexturePath(spriteName);
        Optional<VertexConsumer> spriteBuffer;

        // All overlays are either stitched to the block atlas or an individual texture
        spriteBuffer = makeBufferIfSprite(
                TextureAtlas.LOCATION_BLOCKS,
                overlayLocation,
                spriteName,
                bufferSource,
                renderTypeFunction
        );

        return spriteBuffer.orElseGet(() -> bufferSource.getBuffer(renderTypeFunction.apply(overlayLocation)));
    }

    /**
     * Creates a render buffer if the overlay is a sprite.
     * @param atlasLocation         atlas to search for the overlay texture
     * @param overlayLocation       location of the overlay texture
     * @param spriteName            overlay location as a sprite name
     * @param bufferSource          source of render buffers
     * @param renderTypeFunction    creates a {@link RenderType} given the location of a texture atlas
     * @return render buffer if the overlay is a sprite
     */
    @Unique
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

}