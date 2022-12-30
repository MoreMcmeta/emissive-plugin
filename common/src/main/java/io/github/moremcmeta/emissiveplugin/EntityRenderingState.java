package io.github.moremcmeta.emissiveplugin;

import io.github.moremcmeta.emissiveplugin.mixin.ModelPartMixin;
import net.minecraft.client.renderer.RenderType;

/**
 * Holds global state for the {@link ModelPartMixin}.
 * @author soir20
 */
public class EntityRenderingState {
    public static RenderType currentRenderType;
    public static int partRenderDepth = -1;
}
