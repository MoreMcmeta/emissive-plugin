package io.github.moremcmeta.emissiveplugin.forge;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class OverlayBakedModel extends BakedModelWrapper<BakedModel> {
    private final TextureAtlas ATLAS;

    public OverlayBakedModel(BakedModel originalModel) {
        super(originalModel);
        ATLAS = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)  {
        return makeOverlayQuads(originalModel.getQuads(state, side, rand));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
                                    IModelData extraData)  {
        return makeOverlayQuads(originalModel.getQuads(state, side, rand, extraData));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    private List<BakedQuad> makeOverlayQuads(List<BakedQuad> quads) {
        return quads.stream()
                .filter(
                        (quad) -> MetadataRegistry.INSTANCE
                                .metadataFromSpriteName(ModConstants.DISPLAY_NAME, quad.getSprite().getName())
                                .isPresent()
                ).map(
                        (quad) -> {
                            OverlayMetadata metadata = ((OverlayMetadata) MetadataRegistry.INSTANCE
                                    .metadataFromSpriteName(ModConstants.DISPLAY_NAME, quad.getSprite().getName())
                                    .orElseThrow());

                            TextureAtlasSprite sprite = ATLAS.getSprite(metadata.overlayLocation());

                            return new BakedQuad(
                                    makeVerticesFullBright(
                                            quad.getVertices(),
                                            sprite,
                                            quad.getSprite(),
                                            metadata.isEmissive()
                                    ),
                                    quad.getTintIndex(),
                                    quad.getDirection(),
                                    sprite,
                                    false
                            );
                        }
                )
                .toList();
    }

    private int[] makeVerticesFullBright(int[] vertexData, TextureAtlasSprite newSprite, TextureAtlasSprite oldSprite,
                                         boolean emissive) {
        final int VERTEX_SIZE = 8;
        final int TEX_U_OFFSET = 4;
        final int TEX_V_OFFSET = 5;
        final int LIGHT_OFFSET = 6;

        int[] newVertexData = new int[vertexData.length];
        System.arraycopy(vertexData, 0, newVertexData, 0, vertexData.length);

        for (int vertex = 0; vertex < 4; vertex++) {
            int vertexOffset = vertex * VERTEX_SIZE;
            int texUOffset = vertexOffset + TEX_U_OFFSET;
            int texVOffset = vertexOffset + TEX_V_OFFSET;
            newVertexData[texUOffset] = recomputeSpriteU(newVertexData[texUOffset], oldSprite, newSprite);
            newVertexData[texVOffset] = recomputeSpriteV(newVertexData[texVOffset], oldSprite, newSprite);
            if (emissive) {
                newVertexData[vertexOffset + LIGHT_OFFSET] = LightTexture.FULL_BRIGHT;
            }
        }

        return newVertexData;
    }

    private int recomputeSpriteU(int u, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
        return Float.floatToRawIntBits(
                recomputeSpriteCoordinate(
                        Float.intBitsToFloat(u),
                        oldSprite,
                        newSprite,
                        TextureAtlasSprite::getU0,
                        TextureAtlasSprite::getU1
                )
        );
    }

    private int recomputeSpriteV(int v, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
        return Float.floatToRawIntBits(
                recomputeSpriteCoordinate(
                        Float.intBitsToFloat(v),
                        oldSprite,
                        newSprite,
                        TextureAtlasSprite::getV0,
                        TextureAtlasSprite::getV1
                )
        );
    }

    private float recomputeSpriteCoordinate(float coord, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite,
                                            Function<TextureAtlasSprite, Float> getCoord0,
                                            Function<TextureAtlasSprite, Float> getCoord1) {
        float oldCoord0 = getCoord0.apply(oldSprite);
        float oldCoord1 = getCoord1.apply(oldSprite);
        float proportionInSprite = (coord - oldCoord0) / (oldCoord1 - oldCoord0);
        return Mth.lerp(proportionInSprite, getCoord0.apply(newSprite), getCoord1.apply(newSprite));
    }
}
