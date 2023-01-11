package io.github.moremcmeta.emissiveplugin.forge.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.BakedModelWrapper;

import java.util.ArrayList;
import java.util.List;

public class OverlayBakedItemModel extends BakedModelWrapper<BakedModel> {
    private final Pair<BakedModel, RenderType> OVERLAY_LAYER;

    public OverlayBakedItemModel(BakedModel originalModel) {
        super(originalModel);
        OVERLAY_LAYER = Pair.of(new OverlayBakedModel(originalModel), Sheets.translucentCullBlockSheet());
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
