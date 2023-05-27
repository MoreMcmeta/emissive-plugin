package io.github.moremcmeta.emissiveplugin.fabric.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayOnlyBakedModel;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EmissiveModelBlockRenderer extends ModelBlockRenderer {
    public EmissiveModelBlockRenderer(BlockColors colors) {
        super(colors);
    }

    @Override
    public void renderModel(PoseStack.Pose poseStack, VertexConsumer buffer, @Nullable BlockState state,
                            BakedModel model, float tintR, float tintG, float tintB, int packedLight, int packedOverlay) {
        super.renderModel(poseStack, buffer, state, model, tintR, tintG, tintB, packedLight, packedOverlay);
        super.renderModel(poseStack, buffer, state, new OverlayOnlyBakedModel(model), tintR, tintG, tintB, packedLight,
                packedOverlay);
    }

}
