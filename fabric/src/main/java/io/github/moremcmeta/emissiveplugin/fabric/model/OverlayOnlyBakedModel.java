package io.github.moremcmeta.emissiveplugin.fabric.model;

import io.github.moremcmeta.emissiveplugin.model.OverlayQuadFunction;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Renders only the overlay quads for a {@link BakedModel}.
 * @author soir20
 */
public class OverlayOnlyBakedModel extends ForwardingBakedModel {
    private final OverlayQuadFunction OVERLAY_QUAD_FUNCTION;

    /**
     * Creates a new baked model that only renders the overlay quads for the given model.
     * @param model     original model whose overlay quads need to be rendered
     */
    public OverlayOnlyBakedModel(BakedModel model) {
        wrapped = model;
        OVERLAY_QUAD_FUNCTION = new OverlayQuadFunction();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)  {
        return OVERLAY_QUAD_FUNCTION.apply(wrapped.getQuads(state, side, rand));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}
