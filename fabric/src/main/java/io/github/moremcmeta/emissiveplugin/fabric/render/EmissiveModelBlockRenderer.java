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

package io.github.moremcmeta.emissiveplugin.fabric.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayOnlyBakedModel;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A {@link EmissiveModelBlockRenderer} that can render emissive overlays on blocks.
 * @author soir20
 */
@MethodsReturnNonnullByDefault
public final class EmissiveModelBlockRenderer extends ModelBlockRenderer {

    /**
     * Creates a new emissive block renderer.
     * @param colors    Minecraft's tint colors for various blocks
     */
    public EmissiveModelBlockRenderer(BlockColors colors) {
        super(colors);
    }

    @Override
    public void renderModel(PoseStack.Pose poseStack, VertexConsumer buffer, BlockState state,
                            BakedModel model, float tintR, float tintG, float tintB, int packedLight, int packedOverlay) {
        super.renderModel(poseStack, buffer, state, model, tintR, tintG, tintB, packedLight, packedOverlay);
        super.renderModel(poseStack, buffer, state, new OverlayOnlyBakedModel(model), tintR, tintG, tintB, packedLight,
                packedOverlay);
    }

}
