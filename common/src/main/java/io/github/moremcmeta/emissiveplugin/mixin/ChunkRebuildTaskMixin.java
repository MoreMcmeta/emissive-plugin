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

package io.github.moremcmeta.emissiveplugin.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.render.BaseSpriteFinder;
import io.github.moremcmeta.emissiveplugin.render.OverlayVertexConsumer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Renders overlay quads in the translucent layer after the base fluid was rendered.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public class ChunkRebuildTaskMixin {
    @Unique
    private final BaseSpriteFinder SPRITE_FINDER = new BaseSpriteFinder(
            MetadataRegistry.INSTANCE.metadataByPlugin(ModConstants.MOD_ID)
                    .keySet()
                    .stream()
                    .map(SpriteName::fromTexturePath)
                    .map(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
    );

    /**
     * Renders overlay quads in the translucent layer after the base fluid was rendered.
     * @param x                     player's x-coordinate
     * @param y                     player's y-coordinate
     * @param z                     player's z-coordinate
     * @param compiledChunk         results of chunk compilation
     * @param bufferPack            buffers by render type
     * @param callbackInfo          callback info from Mixin
     * @param unused                unknown variable that does not seem to be used anywhere
     * @param origin                chunk origin
     * @param maxInChunk            maximum position in chunk
     * @param visGraph              visibility graph for rendering
     * @param blockEntities         set of block entities in the chunk
     * @param renderChunkRegion     region being rendered
     * @param poseStack             pose stack for rendering
     * @param random                random number generator
     * @param blockRenderDispatcher handles block and fluid renderers
     * @param posIterator           iterator over all positions in the world being rendered
     * @param currentPos            position of the fluid being rendered
     * @param blockState            block state of the fluid being rendered
     */
    @Inject(method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER, ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void moremcmeta_emissive_onChunkCompile(
            float x, float y, float z, ChunkRenderDispatcher.CompiledChunk compiledChunk,
            ChunkBufferBuilderPack bufferPack, CallbackInfoReturnable<Set<BlockEntity>> callbackInfo, int unused,
            BlockPos origin, BlockPos maxInChunk, VisGraph visGraph, Set<BlockEntity> blockEntities,
            RenderChunkRegion renderChunkRegion, PoseStack poseStack, Random random,
            BlockRenderDispatcher blockRenderDispatcher, Iterator<BlockPos> posIterator, BlockPos currentPos,
            BlockState blockState
    ) {
        RenderType renderType = RenderType.translucent();
        BufferBuilder bufferBuilder = bufferPack.builder(renderType);
        if (compiledChunk.hasLayer.add(renderType)) {

            // Quad vertex mode
            bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);

        }

        VertexConsumer wrappedBuffer = new OverlayVertexConsumer(SPRITE_FINDER, bufferBuilder);
        if (blockRenderDispatcher.renderLiquid(currentPos, renderChunkRegion, wrappedBuffer, renderChunkRegion.getFluidState(currentPos))) {
            compiledChunk.hasBlocks.add(renderType);
        }
    }

}
