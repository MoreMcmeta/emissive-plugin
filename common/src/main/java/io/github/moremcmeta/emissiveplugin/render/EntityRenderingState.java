package io.github.moremcmeta.emissiveplugin.render;

import io.github.moremcmeta.emissiveplugin.mixin.ModelPartMixin;
import net.minecraft.client.renderer.RenderType;

/**
 * Holds global state for the {@link ModelPartMixin}.
 * @author soir20
 */
public class EntityRenderingState {

    /* Entity and block entity rendering should be single-threaded, but use thread locals to
       avoid difficult bugs in case something changes. */
    public static final ThreadLocal<RenderType> currentRenderType = new ThreadLocal<>();
    public static final ThreadLocal<Integer> partRenderDepth = ThreadLocal.withInitial(() -> -1);

}
