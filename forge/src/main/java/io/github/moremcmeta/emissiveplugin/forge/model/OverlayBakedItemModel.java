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

package io.github.moremcmeta.emissiveplugin.forge.model;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an item model and its overlay.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OverlayBakedItemModel extends BakedModelWrapper<BakedModel> {
    private final OverlayQuadFunction OVERLAY_QUAD_FUNCTION;

    /**
     * Creates a new overlay model for items.
     * @param originalModel     original model to wrap
     */
    public OverlayBakedItemModel(BakedModel originalModel) {
        super(originalModel);
        OVERLAY_QUAD_FUNCTION = new OverlayQuadFunction(OverlayBakedQuadForge::new);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        List<BakedQuad> quads = new ArrayList<>(
                super.getQuads(state, side, rand)
        );
        quads.addAll(OVERLAY_QUAD_FUNCTION.apply(quads));

        return quads;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack,
                                     boolean applyLeftHandTransform) {
        return new OverlayBakedItemModel(
                originalModel.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform)
        );
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                    @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (state != null && (renderType == Sheets.translucentCullBlockSheet() || renderType == RenderType.translucent())) {
            List<BakedQuad> quads = new ArrayList<>();
            super.getRenderTypes(state, rand, extraData).forEach(
                    (type) -> quads.addAll(super.getQuads(state, side, rand, extraData, type))
            );

            return OVERLAY_QUAD_FUNCTION.apply(quads);
        }

        return super.getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                             @NotNull ModelData data) {
        return ChunkRenderTypeSet.union(
                ChunkRenderTypeSet.of(RenderType.translucent()),
                super.getRenderTypes(state, rand, data)
        );
    }


    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        List<RenderType> renderTypes = new ArrayList<>(super.getRenderTypes(itemStack, fabulous));
        renderTypes.add(Sheets.translucentCullBlockSheet());
        renderTypes.add(RenderType.translucent());
        return renderTypes;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return originalModel.getRenderPasses(itemStack, fabulous).stream().map(
                (model) -> (BakedModel) new OverlayBakedItemModel(model)
        ).toList();
    }

}
