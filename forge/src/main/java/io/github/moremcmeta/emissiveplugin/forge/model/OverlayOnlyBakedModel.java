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

import io.github.moremcmeta.emissiveplugin.metadata.TransparencyMode;
import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders only the overlay quads for a {@link BakedModel}.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public final class OverlayOnlyBakedModel extends BakedModelWrapper<BakedModel> {
    public static final RenderType TRANSLUCENT_BLOCK_TYPE = RenderType.translucent();
    public static final RenderType SOLID_REPLACEMENT_BLOCK_TYPE = RenderType.cutoutMipped();
    private static final RenderType SOLID_BLOCK_TYPE = RenderType.solid();
    private final OverlayQuadFunction OVERLAY_QUAD_FUNCTION;
    private final RenderType RENDER_TYPE;

    /**
     * Creates a new baked model that only renders the overlay quads for the given model.
     * @param originalModel     original model whose overlay quads need to be rendered
     * @param renderType        render type being rendered
     */
    public OverlayOnlyBakedModel(BakedModel originalModel, RenderType renderType) {
        super(originalModel);
        OVERLAY_QUAD_FUNCTION = new OverlayQuadFunction();
        RENDER_TYPE = renderType;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return makeOverlayQuads(super.getQuads(state, side, rand), state);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
                                             IModelData extraData)  {
        return makeOverlayQuads(super.getQuads(state, side, rand, extraData), state);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    /**
     * Creates overlay quads from the given base quads.
     * @param baseQuads     quads from the base model
     * @param state         state of the block being rendered
     * @return overlay quads
     */
    private List<BakedQuad> makeOverlayQuads(List<BakedQuad> baseQuads, @Nullable BlockState state) {
        List<BakedQuad> includedQuads = new ArrayList<>();

        if (state == null || RENDER_TYPE == null) {
            includedQuads.addAll(OVERLAY_QUAD_FUNCTION.apply(baseQuads));
            return includedQuads;
        }

        if (RENDER_TYPE.equals(TRANSLUCENT_BLOCK_TYPE)) {
            if (ItemBlockRenderTypes.canRenderInLayer(state, TRANSLUCENT_BLOCK_TYPE)) {
                includedQuads.addAll(OVERLAY_QUAD_FUNCTION.apply(baseQuads));
            } else {
                addAllWithMode(baseQuads, TransparencyMode.TRANSLUCENT, includedQuads);
            }
        } else if ((!RENDER_TYPE.equals(SOLID_BLOCK_TYPE) && ItemBlockRenderTypes.canRenderInLayer(state, RENDER_TYPE))
                || (RENDER_TYPE.equals(SOLID_REPLACEMENT_BLOCK_TYPE) && ItemBlockRenderTypes.canRenderInLayer(state, SOLID_BLOCK_TYPE))) {
            addAllWithMode(baseQuads, TransparencyMode.AUTO, includedQuads);
        }

        return includedQuads;
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

}
