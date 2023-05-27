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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OverlayOnlyBakedModel extends BakedModelWrapper<BakedModel> {
    private final OverlayQuadFunction OVERLAY_QUAD_FUNCTION;

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
