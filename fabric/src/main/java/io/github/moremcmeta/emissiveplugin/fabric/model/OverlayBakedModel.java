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

package io.github.moremcmeta.emissiveplugin.fabric.model;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.metadata.TransparencyMode;
import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static io.github.moremcmeta.emissiveplugin.ModConstants.X_OFFSETS;
import static io.github.moremcmeta.emissiveplugin.ModConstants.Y_OFFSETS;
import static io.github.moremcmeta.emissiveplugin.ModConstants.Z_OFFSETS;
import static java.util.Objects.requireNonNull;

/**
 * {@link BakedModel} that renders the original model and its overlay.
 * @author soir20
 */
public final class OverlayBakedModel extends ForwardingBakedModel {
    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    private static final int BLEND_MODES = BlendMode.values().length;
    private static final RenderMaterial[] EMISSIVE_MATERIAL = new RenderMaterial[BLEND_MODES];
    private static final RenderMaterial[] NON_EMISSIVE_MATERIAL = new RenderMaterial[BLEND_MODES];
    static {
        if (RendererAccess.INSTANCE.hasRenderer()) {
            for (int modeOrdinal = 0; modeOrdinal < BLEND_MODES; modeOrdinal++) {
                BlendMode mode = BlendMode.values()[modeOrdinal];

                EMISSIVE_MATERIAL[modeOrdinal] = RENDERER.materialFinder()
                        .blendMode(mode)
                        .emissive(true)
                        .ambientOcclusion(TriState.FALSE)
                        .disableDiffuse(true)
                        .find();
                NON_EMISSIVE_MATERIAL[modeOrdinal] = RENDERER.materialFinder()
                        .blendMode(mode)
                        .find();
            }
        } else {
            LogManager.getLogger().warn("No renderer is present. Overlays will not be rendered.");
        }
    }
    private final ModelManager MODEL_MANAGER = Minecraft.getInstance().getModelManager();

    /**
     * Creates a new overlay model.
     * @param model     original model to wrap
     */
    public OverlayBakedModel(BakedModel model) {
        wrapped = requireNonNull(model, "Baked model cannot be null");
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (!RendererAccess.INSTANCE.hasRenderer()) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        MeshBuilder builder = RENDERER.meshBuilder();
        OverlayQuadTransform transform = new OverlayQuadTransform(
                builder.getEmitter(),
                MODEL_MANAGER.getAtlas(TextureAtlas.LOCATION_BLOCKS),
                state
        );

        context.pushTransform(transform);
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();

        /* The overlay quads must be emitted after the main mesh has been rendered so that they render over
           other translucent quads. */
        if (transform.emittedAny()) {
            context.meshConsumer().accept(builder.build());
        }

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (!RendererAccess.INSTANCE.hasRenderer()) {
            super.emitItemQuads(stack, randomSupplier, context);
            return;
        }

        MeshBuilder builder = RENDERER.meshBuilder();
        OverlayQuadTransform transform = new OverlayQuadTransform(
                builder.getEmitter(),
                MODEL_MANAGER.getAtlas(TextureAtlas.LOCATION_BLOCKS),
                null
        );

        context.pushTransform(transform);
        super.emitItemQuads(stack, randomSupplier, context);
        context.popTransform();

        /* The overlay quads must be emitted after the main mesh has been rendered so that they render over
           other translucent quads. This also fixes an issue where the original item would be invisible
           when an overlay was rendered. */
        if (transform.emittedAny()) {
            context.meshConsumer().accept(builder.build());
        }

    }

    @Override
    public boolean isVanillaAdapter() {

        // Sodium/Indium won't display the overlays if this is true
        return false;

    }

    /**
     * Emits overlay quads given the original quads.
     * @author soir20
     */
    private static class OverlayQuadTransform implements RenderContext.QuadTransform {
        private static final int VERTS_PER_QUAD = 4;
        private final QuadEmitter EMITTER;
        private final TextureAtlas BLOCK_ATLAS;
        private BlockState blockState;
        private boolean isDefaultSolid;
        private boolean emittedAny;

        /**
         * Creates a new overlay transform.
         * @param emitter       emitter to emit overlay quads to
         * @param blockAtlas    texture atlas for block textures
         * @param blockState    block state (or null if not a block)
         */
        public OverlayQuadTransform(QuadEmitter emitter, TextureAtlas blockAtlas, @Nullable BlockState blockState) {
            EMITTER = emitter;
            BLOCK_ATLAS = blockAtlas;
            this.blockState = blockState;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            TextureAtlasSprite baseSprite = spriteFromQuad(quad);
            Optional<OverlayMetadata> metadataOptional = MetadataRegistry.INSTANCE
                    .metadataFromSpriteName(ModConstants.MOD_ID, baseSprite.contents().name())
                    .map(((metadata) -> (OverlayMetadata) metadata));

            if (metadataOptional.isEmpty()) {
                return true;
            }

            EMITTER.copyFrom(quad);

            OverlayMetadata metadata = metadataOptional.get();
            BlendMode blendMode;

            if (metadata.transparencyMode() == TransparencyMode.TRANSLUCENT) {
                blendMode = BlendMode.TRANSLUCENT;
            } else {
                blendMode = quad.material().blendMode();

                if (blockState != null && blendMode == BlendMode.DEFAULT) {
                    isDefaultSolid = ItemBlockRenderTypes.getChunkRenderType(blockState).equals(RenderType.solid());
                    blockState = null;
                }

                if (isDefaultSolid || blendMode == BlendMode.SOLID) {
                    blendMode = BlendMode.CUTOUT_MIPPED;
                }
            }

            EMITTER.material((metadata.isEmissive() ? EMISSIVE_MATERIAL : NON_EMISSIVE_MATERIAL)[blendMode.ordinal()]);

            int facing = quad.lightFace().ordinal();
            TextureAtlasSprite overlaySprite = BLOCK_ATLAS.getSprite(metadata.overlaySpriteName());
            for (int vertexIndex = 0; vertexIndex < VERTS_PER_QUAD; vertexIndex++) {
                float x = EMITTER.x(vertexIndex);
                float y = EMITTER.y(vertexIndex);
                float z = EMITTER.z(vertexIndex);
                EMITTER.pos(vertexIndex, x + X_OFFSETS[facing], y + Y_OFFSETS[facing], z + Z_OFFSETS[facing]);
                EMITTER.uv(
                        vertexIndex,
                        OverlayQuadFunction.recomputeSpriteCoordinate(
                                EMITTER.u(vertexIndex),
                                baseSprite,
                                overlaySprite,
                                TextureAtlasSprite::getU0,
                                TextureAtlasSprite::getU1
                        ),
                        OverlayQuadFunction.recomputeSpriteCoordinate(
                                EMITTER.v(vertexIndex),
                                baseSprite,
                                overlaySprite,
                                TextureAtlasSprite::getV0,
                                TextureAtlasSprite::getV1
                        )
                );
            }

            EMITTER.emit();
            emittedAny = true;
            return true;
        }

        /**
         * Checks if any quads were emitted from this transform.
         * @return whether any quads were emitted
         */
        public boolean emittedAny() {
            return emittedAny;
        }

        /**
         * Gets the sprite used by a given quad.
         * @param quad      quad to get the sprite of
         * @return quad's sprite
         */
        private TextureAtlasSprite spriteFromQuad(QuadView quad) {
            return SpriteFinder.get(BLOCK_ATLAS).find(quad);
        }

    }
}
