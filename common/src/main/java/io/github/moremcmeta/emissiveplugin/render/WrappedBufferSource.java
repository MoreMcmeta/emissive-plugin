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

package io.github.moremcmeta.emissiveplugin.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.Consumer;

/**
 * Wraps render buffer sources so that an action is run when a buffer is retrieved.
 * @author soir20
 */
public final class WrappedBufferSource implements MultiBufferSource {
    private final MultiBufferSource DELEGATE;
    private final Consumer<RenderType> ACTION;

    /**
     * Wraps the given buffer source so that it runs the given action when a buffer is retrieved.
     * @param bufferSource      buffer source to wrap
     * @param action            action to perform when a buffer is retrieved from the source
     * @return wrapped buffer source
     */
    public static MultiBufferSource wrap(MultiBufferSource bufferSource, Consumer<RenderType> action) {
        if (bufferSource instanceof WrappedBufferSource) {
            return bufferSource;
        }

        return new WrappedBufferSource(bufferSource, action);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        ACTION.accept(renderType);
        return DELEGATE.getBuffer(renderType);
    }

    /**
     * Creates a new wrapped buffer source.
     * @param delegate      buffer source to wrap
     * @param action        action to perform when a buffer is retrieved from the source
     */
    private WrappedBufferSource(MultiBufferSource delegate, Consumer<RenderType> action) {
        DELEGATE = delegate;
        ACTION = action;
    }

}
