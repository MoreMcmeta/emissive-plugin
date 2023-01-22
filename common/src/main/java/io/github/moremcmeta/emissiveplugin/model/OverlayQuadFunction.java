package io.github.moremcmeta.emissiveplugin.model;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Function;

public class OverlayQuadFunction implements Function<List<BakedQuad>, List<BakedQuad>> {
    private final ModelManager MODEL_MANAGER;

    public OverlayQuadFunction() {
        MODEL_MANAGER = Minecraft.getInstance().getModelManager();
    }

    public List<BakedQuad> apply(List<BakedQuad> quads) {
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

                            TextureAtlasSprite sprite = MODEL_MANAGER
                                    .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                                    .getSprite(metadata.overlayLocation());

                            /* We have to cast because Minecraft's BakedModel interface expects a List<BakedQuad>,
                               not List<? extends BakedQuad>. */
                            return (BakedQuad) new OverlayBakedQuad(
                                    makeVerticesFullBright(
                                            quad.getVertices(),
                                            sprite,
                                            quad.getSprite(),
                                            metadata.isEmissive()
                                    ),
                                    quad.getTintIndex(),
                                    quad.getDirection(),
                                    sprite,
                                    false,
                                    metadata.isEmissive()
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
