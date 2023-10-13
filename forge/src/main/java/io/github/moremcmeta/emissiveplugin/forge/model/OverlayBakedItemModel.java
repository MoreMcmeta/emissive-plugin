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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.moremcmeta.emissiveplugin.metadata.TransparencyMode;
import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Renders an item model and its overlay.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OverlayBakedItemModel extends BakedModelWrapper<BakedModel> {
    private static final Set<RenderType> TRANSLUCENT_TYPES = ImmutableSet.of(
            RenderType.translucent(),
            Sheets.translucentItemSheet(),
            Sheets.translucentCullBlockSheet()
    );
    private static final Set<RenderType> TRANSLUCENT_BLOCK_TYPES = TRANSLUCENT_TYPES.stream()
            .filter(OverlayBakedItemModel::isBlockType)
            .collect(Collectors.toSet());
    private static final Set<RenderType> TRANSLUCENT_ITEM_TYPES = TRANSLUCENT_TYPES.stream()
            .filter((type) -> !isBlockType(type))
            .collect(Collectors.toSet());
    private static final RenderType SOLID_BLOCK_TYPE = RenderType.solid();
    private static final RenderType SOLID_REPLACEMENT_BLOCK_TYPE = RenderType.cutoutMipped();
    private static final ChunkRenderTypeSet TRANSLUCENT_BLOCK_TYPES_SET = ChunkRenderTypeSet.of(TRANSLUCENT_BLOCK_TYPES);
    private static final ChunkRenderTypeSet SOLID_REPLACEMENT_BLOCK_TYPE_SET = ChunkRenderTypeSet.of(SOLID_REPLACEMENT_BLOCK_TYPE);

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
    public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack,
                                     boolean applyLeftHandTransform) {
        return new OverlayBakedItemModel(
                originalModel.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform)
        );
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                    @NotNull ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> baseQuads = super.getQuads(state, side, rand, extraData, renderType);
        List<BakedQuad> includedQuads = new ArrayList<>();

        if (state == null || renderType == null) {
            includedQuads.addAll(baseQuads);
            includedQuads.addAll(OVERLAY_QUAD_FUNCTION.apply(baseQuads));
            return includedQuads;
        }

        ChunkRenderTypeSet types = super.getRenderTypes(state, rand, extraData);

        if (types.contains(renderType)) {
            includedQuads.addAll(baseQuads);
        }

        if (TRANSLUCENT_BLOCK_TYPES_SET.contains(renderType)) {
            types.forEach((type) -> {
                List<BakedQuad> typeBaseQuads = super.getQuads(state, side, rand, extraData, type);

                // Each model should have exactly one translucent type, so no duplicates will be added
                if (TRANSLUCENT_BLOCK_TYPES_SET.contains(type)) {
                    includedQuads.addAll(OVERLAY_QUAD_FUNCTION.apply(typeBaseQuads));
                } else {
                    addAllWithMode(typeBaseQuads, TransparencyMode.TRANSLUCENT, includedQuads);
                }

            });
        } else if (!renderType.equals(SOLID_BLOCK_TYPE)) {
            addAllWithMode(includedQuads, TransparencyMode.AUTO, includedQuads);

            if (types.contains(SOLID_BLOCK_TYPE) && renderType.equals(SOLID_REPLACEMENT_BLOCK_TYPE)) {
                addAllWithMode(
                        super.getQuads(state, side, rand, extraData, SOLID_BLOCK_TYPE),
                        TransparencyMode.AUTO,
                        includedQuads
                );
            }
        }

        return includedQuads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                             @NotNull ModelData data) {
        return ChunkRenderTypeSet.union(
                TRANSLUCENT_BLOCK_TYPES_SET,
                SOLID_REPLACEMENT_BLOCK_TYPE_SET,
                super.getRenderTypes(state, rand, data)
        );
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        List<RenderType> renderTypes = super.getRenderTypes(itemStack, fabulous);

        if (renderTypes.stream().anyMatch(TRANSLUCENT_ITEM_TYPES::contains)) {
            return renderTypes;
        }

        RenderType translucentType = fabulous || !Minecraft.useShaderTransparency()
                ? Sheets.translucentCullBlockSheet()
                : Sheets.translucentItemSheet();
        return ImmutableList.of(translucentType);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return originalModel.getRenderPasses(itemStack, fabulous).stream().map(
                (model) -> (BakedModel) new OverlayBakedItemModel(model)
        ).toList();
    }

    /**
     * Computes overlay quads and adds those that have the given transparency mode to the results list.
     * @param quads             base quads
     * @param transparencyMode  transparency mode to filter the quads
     * @param results           results to which to add matching quads
     */
    private void addAllWithMode(List<BakedQuad> quads, TransparencyMode transparencyMode, List<BakedQuad> results) {
        OVERLAY_QUAD_FUNCTION.apply(quads).stream()
                .filter((quad) -> quad.transparencyMode() == transparencyMode)
                .forEach(results::add);
    }

    /**
     * Checks if a render type is a block/chunk type.
     * @param type      type to check
     * @return whether the render type is a block type
     */
    private static boolean isBlockType(RenderType type) {
        return type.getChunkLayerId() >= 0;
    }

}
