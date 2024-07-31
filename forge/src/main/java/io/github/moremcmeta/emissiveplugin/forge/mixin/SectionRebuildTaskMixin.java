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

package io.github.moremcmeta.emissiveplugin.forge.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import io.github.moremcmeta.emissiveplugin.fabricapi.SpriteFinder;
import io.github.moremcmeta.emissiveplugin.mixinaccess.SpriteFinderSupplier;
import io.github.moremcmeta.emissiveplugin.render.LiquidOverlayVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

/**
 * Renders overlay quads in the translucent layer after the base fluid was rendered.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = SectionCompiler.class, priority = Integer.MAX_VALUE)
public final class SectionRebuildTaskMixin {

    /**
     * Renders overlay quads in the translucent layer after the base fluid was rendered.
     * @param sectionPos            section of the chunk
     * @param renderChunkRegion     region being rendered
     * @param vertexSorting         vertex sorter
     * @param bufferPack            buffers by render type
     * @param callbackInfo          callback info from Mixin
     * @param modelDataMap          maps block pos to model data
     * @param compileResults        results of chunk compilation
     * @param chunkOrigin           pos representing the origin of the chunk
     * @param chunkMax              max pos within the chunk
     * @param visGraph              visibility graph for rendering
     * @param poseStack             pose stack for rendering
     * @param chunkBufferLayers     render buffers by chunk layers
     * @param randomSource          source of random number generators
     * @param chunkPosIterator      iterator for all pos in the chunk
     * @param currentPos            position of the fluid being rendered
     * @param state                 block state of the fluid being rendered
     */
    @Inject(method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;" +
                    "renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;" +
                    "Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;" +
                    "Lnet/minecraft/world/level/material/FluidState;)V",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void moremcmeta_emissive_onChunkCompile(
            SectionPos sectionPos, RenderChunkRegion renderChunkRegion, VertexSorting vertexSorting,
            SectionBufferBuilderPack bufferPack, CallbackInfoReturnable<SectionCompiler.Results> callbackInfo,
            Map<BlockPos, ModelData> modelDataMap,
            SectionCompiler.Results compileResults, BlockPos chunkOrigin, BlockPos chunkMax, VisGraph visGraph,
            PoseStack poseStack, Map<RenderType, BufferBuilder> chunkBufferLayers, RandomSource randomSource,
            Iterator<BlockPos> chunkPosIterator, BlockPos currentPos, BlockState state
    ) {
        RenderType renderType = RenderType.translucent();

        @SuppressWarnings("DataFlowIssue")
        SectionCompiler sectionCompiler = ((SectionCompiler) (Object) this);

        BufferBuilder bufferBuilder = sectionCompiler.getOrBeginLayer(chunkBufferLayers, bufferPack, renderType);

        TextureAtlas blockAtlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        SpriteFinder spriteFinder = ((SpriteFinderSupplier) blockAtlas).moremcmeta_emissive_spriteFinder();

        VertexConsumer wrappedBuffer = new LiquidOverlayVertexConsumer(spriteFinder, bufferBuilder);
        sectionCompiler.blockRenderer.renderLiquid(currentPos, renderChunkRegion, wrappedBuffer, state, state.getFluidState());
    }

}
