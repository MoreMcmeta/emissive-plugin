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

import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

/**
 * Renders only the overlay quads for a {@link BakedModel}.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OverlayOnlyBakedModel extends BakedModelWrapper<BakedModel> {
    private final OverlayQuadFunction OVERLAY_QUAD_FUNCTION;

    /**
     * Creates a new baked model that only renders the overlay quads for the given model.
     * @param originalModel     original model whose overlay quads need to be rendered
     */
    public OverlayOnlyBakedModel(BakedModel originalModel) {
        super(originalModel);
        OVERLAY_QUAD_FUNCTION = new OverlayQuadFunction();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)  {
        return OVERLAY_QUAD_FUNCTION.apply(originalModel.getQuads(state, side, rand));
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
                                             IModelData extraData)  {
        return OVERLAY_QUAD_FUNCTION.apply(originalModel.getQuads(state, side, rand, extraData));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }
}
