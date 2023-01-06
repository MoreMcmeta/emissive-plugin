package io.github.moremcmeta.emissiveplugin.fabric;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class OverlayBakedModel extends ForwardingBakedModel {
    private static final RenderMaterial EMISSIVE_MATERIAL;
    private static final RenderMaterial NON_EMISSIVE_MATERIAL;
    static {
        if (RendererAccess.INSTANCE.hasRenderer()) {
            EMISSIVE_MATERIAL = RendererAccess.INSTANCE.getRenderer()
                    .materialFinder()
                    .blendMode(0, BlendMode.TRANSLUCENT)
                    .emissive(0, true)
                    .disableAo(0, true)
                    .disableDiffuse(0, true)
                    .find();
            NON_EMISSIVE_MATERIAL = RendererAccess.INSTANCE.getRenderer()
                    .materialFinder()
                    .blendMode(0, BlendMode.TRANSLUCENT)
                    .find();
        } else {
            LogManager.getLogger().warn("No renderer is present. Overlays may not render correctly.");
            EMISSIVE_MATERIAL = null;
            NON_EMISSIVE_MATERIAL = null;
        }
    }
    private final TextureAtlas BLOCK_ATLAS = Minecraft.getInstance().getModelManager()
            .getAtlas(TextureAtlas.LOCATION_BLOCKS);

    public OverlayBakedModel(BakedModel model) {
        wrapped = requireNonNull(model, "Baked model cannot be null");
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<Random> randomSupplier, RenderContext context) {
        context.pushTransform(new OverlayQuadTransform(context.getEmitter(), BLOCK_ATLAS));
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.pushTransform(new OverlayQuadTransform(context.getEmitter(), BLOCK_ATLAS));
        super.emitItemQuads(stack, randomSupplier, context);
        context.popTransform();
    }

    @Override
    public boolean isVanillaAdapter() {

        // Sodium/Indium won't display the overlays if this is true
        return false;

    }

    private static class OverlayQuadTransform implements RenderContext.QuadTransform {
        private final QuadEmitter EMITTER;
        private final TextureAtlas BLOCK_ATLAS;
        private boolean didJustEmit;

        public OverlayQuadTransform(QuadEmitter emitter, TextureAtlas blockAtlas) {
            EMITTER = requireNonNull(emitter, "Emitter cannot be null");
            BLOCK_ATLAS = requireNonNull(blockAtlas, "Block atlas cannot be null");
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            Optional<OverlayMetadata> metadataOptional = MetadataRegistry.INSTANCE
                    .metadataFromSpriteName(ModConstants.DISPLAY_NAME, spriteFromQuad(quad).getName())
                    .map(((metadata) -> (OverlayMetadata) metadata));

            // Avoid a stack overflow by not applying this transform to quads emitted from this transform
            if (metadataOptional.isEmpty() || didJustEmit) {
                return true;
            }

            quad.copyTo(EMITTER);

            OverlayMetadata metadata = metadataOptional.get();
            if (RendererAccess.INSTANCE.hasRenderer()) {
                EMITTER.material(metadata.isEmissive() ? EMISSIVE_MATERIAL : NON_EMISSIVE_MATERIAL);
            }

            EMITTER.spriteBake(
                    0,
                    BLOCK_ATLAS.getSprite(metadata.overlayLocation()),
                    MutableQuadView.BAKE_LOCK_UV
            );

            didJustEmit = true;
            EMITTER.emit();
            didJustEmit = false;

            return true;
        }

        private TextureAtlasSprite spriteFromQuad(QuadView quad) {
            return SpriteFinder.get(BLOCK_ATLAS).find(quad, 0);
        }
    }
}
