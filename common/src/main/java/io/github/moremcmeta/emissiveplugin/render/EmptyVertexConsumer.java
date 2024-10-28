/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2024 soir20
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
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.lwjgl.system.MemoryStack;

import java.util.function.Supplier;

/**
 * Vertex consumer that renders nothing.
 * @author soir20
 */
@MethodsReturnNonnullByDefault
public class EmptyVertexConsumer implements VertexConsumer {
    private static final Supplier<EmptyVertexConsumer> FACTORY;
    private static final ThreadLocal<EmptyVertexConsumer> instance = new ThreadLocal<>();
    static {
        Supplier<EmptyVertexConsumer> factory;
        try {
            // Check whether the Sodium 0.6+ buffer interface is present before attempting to load the class
            Class.forName("net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter");
            factory = () -> {
                if (instance.get() == null) {
                    instance.set(new SodiumEmptyVertexConsumer());
                }
                return instance.get();
            };
        } catch (ClassNotFoundException err) {
            factory = () -> {
                if (instance.get() == null) {
                    instance.set(new EmptyVertexConsumer());
                }
                return instance.get();
            };
        }
        FACTORY = factory;
    }

    /**
     * Create a new no-op vertex consumer.
     * @return no-op vertex consumer
     */
    public static EmptyVertexConsumer create() {
        return FACTORY.get();
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this;
    }

    /**
     * Creates new no-op vertex consumer.
     */
    private EmptyVertexConsumer() {}

    /**
     * Creates a new no-op vertex consumer compatible with Sodium 0.6+.
     * @author soir20
     */
    private static final class SodiumEmptyVertexConsumer extends EmptyVertexConsumer implements VertexBufferWriter {

        /**
         * The other push() method is used in older Sodium betas. Add this method for
         * compatibility with newer Sodium versions.
         * @param memoryStack       scratch memory
         * @param pointer           pointer to read vertices from
         * @param numVertices       number of vertices to read
         * @param vertexFormat      format of vertices to push
         */
        @SuppressWarnings("unused")
        public void push(MemoryStack memoryStack, long pointer, int numVertices, VertexFormat vertexFormat) {}

        @Override
        public void push(MemoryStack memoryStack, long pointer, int numVertices, VertexFormatDescription vertexFormatDescription) {}
    }
}
