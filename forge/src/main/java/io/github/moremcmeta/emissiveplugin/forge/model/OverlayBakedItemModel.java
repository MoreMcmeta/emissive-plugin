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
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.BakedModelWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an item model and its overlay.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public final class OverlayBakedItemModel extends BakedModelWrapper<BakedModel> {
    private final Pair<BakedModel, RenderType> OVERLAY_LAYER;

    /**
     * Creates a new overlay model for items.
     * @param originalModel     original model to wrap
     */
    public OverlayBakedItemModel(BakedModel originalModel) {
        super(originalModel);
        RenderType renderType = Sheets.translucentCullBlockSheet();
        OVERLAY_LAYER = Pair.of(
                new OverlayOnlyBakedModel(originalModel, renderType),
                renderType
        );
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return new OverlayBakedItemModel(super.handlePerspective(cameraTransformType, poseStack));
    }

    @Override
    public boolean isLayered() {
        return true;
    }

    @Override
    public List<Pair<BakedModel, RenderType>> getLayerModels(ItemStack itemStack, boolean fabulous) {
        List<Pair<BakedModel, RenderType>> layers = new ArrayList<>(super.getLayerModels(itemStack, fabulous));
        layers.add(OVERLAY_LAYER);
        return layers;
    }

}
