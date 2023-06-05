package io.github.moremcmeta.emissiveplugin.fabric.model;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * {@link BakedModel} that renders the original model and its overlay.
 * @author soir20
 */
public class OverlayBakedModel extends ForwardingBakedModel {
    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    private static final RenderMaterial EMISSIVE_MATERIAL;
    private static final RenderMaterial NON_EMISSIVE_MATERIAL;
    static {
        if (RendererAccess.INSTANCE.hasRenderer()) {
            EMISSIVE_MATERIAL = RENDERER.materialFinder()
                    .blendMode(0, BlendMode.TRANSLUCENT)
                    .emissive(0, true)
                    .disableAo(0, true)
                    .disableDiffuse(0, true)
                    .find();
            NON_EMISSIVE_MATERIAL = RENDERER.materialFinder()
                    .blendMode(0, BlendMode.TRANSLUCENT)
                    .find();
        } else {
            LogManager.getLogger().warn("No renderer is present. Overlays will not be rendered.");
            EMISSIVE_MATERIAL = null;
            NON_EMISSIVE_MATERIAL = null;
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
                               Supplier<Random> randomSupplier, RenderContext context) {
        if (!RendererAccess.INSTANCE.hasRenderer()) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        MeshBuilder builder = RENDERER.meshBuilder();
        OverlayQuadTransform transform = new OverlayQuadTransform(
                builder.getEmitter(),
                MODEL_MANAGER.getAtlas(TextureAtlas.LOCATION_BLOCKS)
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
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        if (!RendererAccess.INSTANCE.hasRenderer()) {
            super.emitItemQuads(stack, randomSupplier, context);
            return;
        }

        MeshBuilder builder = RENDERER.meshBuilder();
        OverlayQuadTransform transform = new OverlayQuadTransform(
                builder.getEmitter(),
                MODEL_MANAGER.getAtlas(TextureAtlas.LOCATION_BLOCKS)
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
        private final QuadEmitter EMITTER;
        private final TextureAtlas BLOCK_ATLAS;
        private boolean emittedAny;

        /**
         * Creates a new overlay transform.
         * @param emitter       emitter to emit overlay quads to
         * @param blockAtlas    texture atlas for block textures
         */
        public OverlayQuadTransform(QuadEmitter emitter, TextureAtlas blockAtlas) {
            EMITTER = requireNonNull(emitter, "Emitter cannot be null");
            BLOCK_ATLAS = requireNonNull(blockAtlas, "Block atlas cannot be null");
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            Optional<OverlayMetadata> metadataOptional = MetadataRegistry.INSTANCE
                    .metadataFromSpriteName(ModConstants.MOD_ID, spriteFromQuad(quad).getName())
                    .map(((metadata) -> (OverlayMetadata) metadata));

            if (metadataOptional.isEmpty()) {
                return true;
            }

            quad.copyTo(EMITTER);

            OverlayMetadata metadata = metadataOptional.get();
            EMITTER.material(metadata.isEmissive() ? EMISSIVE_MATERIAL : NON_EMISSIVE_MATERIAL);

            EMITTER.spriteBake(
                    0,
                    BLOCK_ATLAS.getSprite(metadata.overlaySpriteName()),
                    MutableQuadView.BAKE_LOCK_UV
            );

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
            return SpriteFinder.get(BLOCK_ATLAS).find(quad, 0);
        }

    }
}
