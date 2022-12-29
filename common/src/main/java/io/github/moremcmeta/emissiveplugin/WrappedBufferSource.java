/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.moremcmeta.emissiveplugin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.Consumer;

public final class WrappedBufferSource implements MultiBufferSource {
    public static RenderType currentRenderType;
    public static int depth = -1;

    private final MultiBufferSource DELEGATE;
    private final Consumer<RenderType> ACTION;

    public static MultiBufferSource wrap(MultiBufferSource bufferSource, Consumer<RenderType> action) {
        if (bufferSource instanceof WrappedBufferSource) {
            return bufferSource;
        }

        return new WrappedBufferSource(bufferSource, action);
    }

    public WrappedBufferSource(MultiBufferSource delegate, Consumer<RenderType> action) {
        DELEGATE = delegate;
        ACTION = action;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        ACTION.accept(renderType);
        return DELEGATE.getBuffer(renderType);
    }
}
