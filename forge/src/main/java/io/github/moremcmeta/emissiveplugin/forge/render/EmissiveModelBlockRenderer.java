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

package io.github.moremcmeta.emissiveplugin.forge.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.forge.model.OverlayOnlyBakedModel;
import net.minecraft.MethodsReturnNonnullByDefault;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

/**
 * A {@link EmissiveModelBlockRenderer} that can render emissive overlays on blocks.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class EmissiveModelBlockRenderer extends ForgeBlockModelRenderer {
    public static final ThreadLocal<Boolean> ALWAYS_RENDER_ON_TRANSPARENCY = ThreadLocal.withInitial(() -> true);

    /**
     * Creates a new emissive block renderer.
     * @param colors    Minecraft's tint colors for various blocks
     */
    public EmissiveModelBlockRenderer(BlockColors colors) {
        super(colors);
    }

    @Override
    public boolean tesselateWithAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
                                   PoseStack poseStack, VertexConsumer buffer, boolean checkSides, Random rand,
                                   long seed, int packedOverlay, IModelData modelData) {
        boolean didRender = false;
        RenderType renderType = MinecraftForgeClient.getRenderLayer();

        ALWAYS_RENDER_ON_TRANSPARENCY.set(false);
        if (ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
            didRender = super.tesselateWithAO(level, model, state, pos, poseStack, buffer, checkSides, rand, seed,
                    packedOverlay, modelData);
        }
        ALWAYS_RENDER_ON_TRANSPARENCY.set(true);

        if (renderType == RenderType.translucent()) {
            boolean didOverlayRender = super.tesselateWithoutAO(level, new OverlayOnlyBakedModel(model), state, pos,
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
        RenderType renderType = MinecraftForgeClient.getRenderLayer();

        ALWAYS_RENDER_ON_TRANSPARENCY.set(false);
        if (ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
            didRender = super.tesselateWithoutAO(level, model, state, pos, poseStack, buffer, checkSides, rand, seed,
                    packedOverlay, modelData);
        }
        ALWAYS_RENDER_ON_TRANSPARENCY.set(true);

        if (renderType == RenderType.translucent()) {
            boolean didOverlayRender = super.tesselateWithoutAO(level, new OverlayOnlyBakedModel(model), state, pos,
                    poseStack, buffer, checkSides, rand, seed, packedOverlay, modelData);
            didRender = didRender || didOverlayRender;
        }
        return didRender;
    }

    @Override
    public void renderModel(PoseStack.Pose poseStack, VertexConsumer buffer, @Nullable BlockState state,
                            BakedModel model, float tintR, float tintG, float tintB, int packedLight, int packedOverlay,
                            IModelData modelData) {
        super.renderModel(poseStack, buffer, state, model, tintR, tintG, tintB, packedLight, packedOverlay, modelData);
        super.renderModel(poseStack, buffer, state, new OverlayOnlyBakedModel(model), tintR, tintG, tintB, packedLight,
                packedOverlay, modelData);
    }

}
