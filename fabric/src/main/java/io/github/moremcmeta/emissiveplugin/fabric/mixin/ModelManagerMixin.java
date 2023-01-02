package io.github.moremcmeta.emissiveplugin.fabric.mixin;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
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

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            method = "apply")
    @SuppressWarnings("ConstantConditions")
    public void onReloaded(CallbackInfo info) {
        Map<ResourceLocation, BakedModel> models = ((ModelManager) (Object) this).bakedRegistry;
        models.forEach(((location, bakedModel) -> models.put(location, new Wrapper(bakedModel))));
    }

    private static class Wrapper extends ForwardingBakedModel {
        private final TextureAtlas BLOCK_ATLAS = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS);

        private final RenderContext.QuadTransform OVERLAY_TRANSFORM = (quad) -> {
            Optional<OverlayMetadata> metadataOptional = MetadataRegistry.INSTANCE
                    .metadataFromSpriteName(ModConstants.DISPLAY_NAME, spriteFromQuad(quad).getName())
                    .map(((metadata) -> (OverlayMetadata) metadata));

            if (metadataOptional.isEmpty()) {
                return false;
            }

            OverlayMetadata metadata = metadataOptional.get();
            if (RendererAccess.INSTANCE.hasRenderer()) {
                quad.material(metadata.isEmissive() ? EMISSIVE_MATERIAL : NON_EMISSIVE_MATERIAL);
            }

            quad.spriteBake(0, BLOCK_ATLAS.getSprite(metadata.overlayLocation()), MutableQuadView.BAKE_LOCK_UV);

            return true;
        };

        public Wrapper(BakedModel model) {
            wrapped = model;
        }

        @Override
        public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                                   Supplier<Random> randomSupplier, RenderContext context) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            context.pushTransform(OVERLAY_TRANSFORM);
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            context.popTransform();
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            super.emitItemQuads(stack, randomSupplier, context);
            context.pushTransform(OVERLAY_TRANSFORM);
            super.emitItemQuads(stack, randomSupplier, context);
            context.popTransform();
        }

        private TextureAtlasSprite spriteFromQuad(QuadView quad) {
            return SpriteFinder.get(BLOCK_ATLAS).find(quad, 0);
        }
    }

}
