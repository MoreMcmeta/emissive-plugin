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

package io.github.moremcmeta.emissiveplugin.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.function.Function;

/**
 * Wraps render buffer sources so that an action is run when a buffer is retrieved.
 * @author soir20
 */
@MethodsReturnNonnullByDefault
public final class WrappedBufferSource implements MultiBufferSource {
    private final Minecraft MINECRAFT = Minecraft.getInstance();
    private final MultiBufferSource DELEGATE;
    private final boolean IS_EMISSIVE;
    private final boolean IS_BLOCK_ENTITY;

    /**
     * Wraps the given buffer source so that it runs the given action when a buffer is retrieved.
     * @param bufferSource      buffer source to wrap
     * @param isEmissive        whether emissive quads should be rendered
     * @param isBlockEntity     whether a block entity is being rendered
     * @return wrapped buffer source
     */
    public static MultiBufferSource wrap(MultiBufferSource bufferSource, boolean isEmissive, boolean isBlockEntity) {
        MultiBufferSource newBufferSource;
        if (bufferSource instanceof WrappedBufferSource) {
            newBufferSource = bufferSource;
        } else {
            newBufferSource = new WrappedBufferSource(bufferSource, isEmissive, isBlockEntity);
        }

        EntityRenderingState.currentBufferSource.set(newBufferSource);
        return newBufferSource;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        Optional<AnalyzedMetadata> metadataOptional = Optional.empty();

        if (renderType instanceof RenderType.CompositeRenderType compositeType
                && compositeType.state().textureState.cutoutTexture().isPresent()) {
            ResourceLocation location = compositeType.state().textureState.cutoutTexture().get();
            metadataOptional = MetadataRegistry.INSTANCE.metadataFromPath(ModConstants.MOD_ID, location);
        }

        return metadataOptional
                .map((analyzedMetadata) -> bufferFromMetadata((OverlayMetadata) analyzedMetadata))
                .orElseGet(EmptyVertexConsumer::new);

    }

    /**
     * Obtains a render buffer directly from {@link OverlayMetadata}.
     * @param overlayMetadata       metadata for the texture whose overlay is being rendered
     * @return render buffer for the overlay
     */
    public VertexConsumer bufferFromMetadata(OverlayMetadata overlayMetadata) {
        ResourceLocation overlay = overlayMetadata.overlaySpriteName();

        if (overlayMetadata.isEmissive() != IS_EMISSIVE) {
            return new EmptyVertexConsumer();
        }

        /* Disabling cull is needed to render emissive layers inside the slime properly, but it needs to
           be enabled for bed overlays to render properly. Z-layering needs to be enabled for armor overlays
           to render properly, so the entity shadow type is used. */
        Function<ResourceLocation, RenderType> overlayType = IS_BLOCK_ENTITY
                ? RenderType::entityTranslucentCull
                : CustomRenderTypes::entityTranslucentZLayering;

        return makeBuffer(DELEGATE, overlay, overlayType);
    }

    /**
     * Creates a buffer to render an overlay texture.
     * @param bufferSource          source of buffers for rendering
     * @param spriteName            name of the overlay texture as a sprite
     * @param renderTypeFunction    creates a render type given the location of a texture used while rendering
     * @return buffer to render the overlay texture
     */
    private VertexConsumer makeBuffer(MultiBufferSource bufferSource, ResourceLocation spriteName,
                                      Function<ResourceLocation, RenderType> renderTypeFunction) {
        ResourceLocation overlayLocation = SpriteName.toTexturePath(spriteName);
        Optional<VertexConsumer> spriteBuffer = makeBufferIfSprite(
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
    private Optional<VertexConsumer> makeBufferIfSprite(ResourceLocation atlasLocation,
                                                        ResourceLocation overlayLocation,
                                                        ResourceLocation spriteName,
                                                        MultiBufferSource bufferSource,
                                                        Function<ResourceLocation, RenderType> renderTypeFunction) {
        AbstractTexture abstractTexture = MINECRAFT.getTextureManager().getTexture(atlasLocation, null);
        if (!(abstractTexture instanceof TextureAtlas atlas)) {
            LogManager.getLogger().warn(
                    "Atlas {} is not a subclass of TextureAtlas; sprites from this atlas will not be used as overlays",
                    atlasLocation
            );
            return Optional.empty();
        }

        TextureAtlasSprite sprite = atlas.getSprite(spriteName);

        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            sprite = atlas.getSprite(overlayLocation);
        }

        if (!sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            RenderType renderType = renderTypeFunction.apply(atlasLocation);
            return Optional.of(sprite.wrap(bufferSource.getBuffer(renderType)));
        }

        return Optional.empty();
    }

    /**
     * Creates a new wrapped buffer source.
     * @param delegate          buffer source to wrap
     * @param isEmissive        whether emissive quads should be rendered
     * @param isBlockEntity     whether a block entity is being rendered
     */
    private WrappedBufferSource(MultiBufferSource delegate, boolean isEmissive, boolean isBlockEntity) {
        DELEGATE = delegate;
        IS_EMISSIVE = isEmissive;
        IS_BLOCK_ENTITY = isBlockEntity;
    }

}
