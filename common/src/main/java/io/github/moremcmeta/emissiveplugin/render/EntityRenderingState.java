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

import io.github.moremcmeta.emissiveplugin.mixin.ModelPartMixin;
import net.minecraft.client.renderer.RenderType;

/**
 * Holds global state for the {@link ModelPartMixin}.
 * @author soir20
 */
public final class EntityRenderingState {

    /* Entity and block entity rendering should be single-threaded, but use thread locals to
       avoid difficult bugs in case something changes. */
    public static final ThreadLocal<RenderType> currentRenderType = new ThreadLocal<>();
    public static final ThreadLocal<Integer> partRenderDepth = ThreadLocal.withInitial(() -> -1);
    public static final ThreadLocal<Boolean> isBlockEntity = ThreadLocal.withInitial(() -> false);

}
