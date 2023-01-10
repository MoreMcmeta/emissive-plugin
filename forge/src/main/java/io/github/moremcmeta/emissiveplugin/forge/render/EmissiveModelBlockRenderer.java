package io.github.moremcmeta.emissiveplugin.forge.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.forge.model.OverlayBakedModel;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;

import java.util.Random;

public class EmissiveModelBlockRenderer extends ForgeBlockModelRenderer {
    public static final ThreadLocal<Boolean> ALWAYS_RENDER_ON_TRANSPARENCY = ThreadLocal.withInitial(() -> true);

    public EmissiveModelBlockRenderer(BlockColors colors) {
        super(colors);
    }

    @Override
    public boolean tesselateWithAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
                                   PoseStack poseStack, VertexConsumer buffer, boolean checkSides, Random rand,
                                   long seed, int packedOverlay, IModelData modelData) {
        boolean didRender = false;
        RenderType renderType = MinecraftForgeClient.getRenderType();

        ALWAYS_RENDER_ON_TRANSPARENCY.set(false);
        if (ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
            didRender = super.tesselateWithAO(level, model, state, pos, poseStack, buffer, checkSides, rand, seed,
                    packedOverlay, modelData);
        }
        ALWAYS_RENDER_ON_TRANSPARENCY.set(true);

        if (renderType == RenderType.translucent()) {
            boolean didOverlayRender = super.tesselateWithoutAO(level, new OverlayBakedModel(model), state, pos,
                    poseStack, buffer, checkSides, rand, seed, packedOverlay, modelData);
            didRender = didRender || didOverlayRender;
        }
        return didRender;
    }

    @Override
    public boolean tesselateWithoutAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
                                      PoseStack poseStack, VertexConsumer buffer, boolean checkSides, Random rand,
                                      long seed, int packedOverlay, IModelData modelData) {
        boolean didRender = false;
        RenderType renderType = MinecraftForgeClient.getRenderType();

        ALWAYS_RENDER_ON_TRANSPARENCY.set(false);
        if (ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
            didRender = super.tesselateWithoutAO(level, model, state, pos, poseStack, buffer, checkSides, rand, seed,
                    packedOverlay, modelData);
        }
        ALWAYS_RENDER_ON_TRANSPARENCY.set(true);

        if (renderType == RenderType.translucent()) {
            boolean didOverlayRender = super.tesselateWithoutAO(level, new OverlayBakedModel(model), state, pos,
                    poseStack, buffer, checkSides, rand, seed, packedOverlay, modelData);
            didRender = didRender || didOverlayRender;
        }
        return didRender;
    }

}
