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

package io.github.moremcmeta.emissiveplugin;

import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Constants for both Fabric and Forge implementations of the plugin.
 * @author soir20
 */
public final class ModConstants {
    public static final String MOD_ID = "moremcmeta_emissive_plugin";
    public static final String SECTION_NAME = "overlay";
    public static final MetadataAnalyzer ANALYZER = new OverlayMetadataAnalyzer();
    public static final ComponentBuilder COMPONENT_BUILDER = (metadata, frames) -> new TextureComponent<CurrentFrameView>() {};
    public static final int FULL_BRIGHT = LightTexture.pack(15, 15);
    public static final Consumer<Map<ResourceLocation, List<Material>>> SPRITE_REGISTRAR = (spritesByAtlas) -> {
        List<Material> sprites = spritesByAtlas.computeIfAbsent(
                TextureAtlas.LOCATION_BLOCKS,
                (ignored) -> new ArrayList<>()
        );
        Set<ResourceLocation> spriteTextures = spritesByAtlas
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(Material::texture)
                .collect(Collectors.toSet());

        MetadataRegistry.INSTANCE.metadataByPlugin(ModConstants.MOD_ID).forEach(
                (textureLocation, metadata) -> {
                    if (spriteTextures.contains(SpriteName.fromTexturePath(textureLocation))) {
                        sprites.add(new Material(
                                TextureAtlas.LOCATION_BLOCKS,
                                ((OverlayMetadata) metadata).overlaySpriteName()
                        ));
                    }
                }
        );
    };
    public static final BiFunction<ModelBakery, UnbakedModel, Boolean> USES_OVERLAY = (bakery, original) -> {
        Set<Pair<String, String>> missingTextures = new HashSet<>();
        return original.getMaterials(bakery::getModel, missingTextures)
                .stream()
                .map(Material::texture)
                .anyMatch((spriteName) -> MetadataRegistry.INSTANCE.metadataFromSpriteName(
                        ModConstants.MOD_ID,
                        spriteName
                ).isPresent());
    };
    private static final int DIRECTIONS = Direction.values().length;
    private static final float OFFSET_COEFFICIENT = 0.001f;
    public static final float[] X_OFFSETS = new float[DIRECTIONS];
    public static final float[] Y_OFFSETS = new float[DIRECTIONS];
    public static final float[] Z_OFFSETS = new float[DIRECTIONS];
    static {
        Direction[] values = Direction.values();
        for (int ordinal = 0; ordinal < DIRECTIONS; ordinal++) {
            Direction value = values[ordinal];
            X_OFFSETS[ordinal] = OFFSET_COEFFICIENT * value.getStepX();
            Y_OFFSETS[ordinal] = OFFSET_COEFFICIENT * value.getStepY();
            Z_OFFSETS[ordinal] = OFFSET_COEFFICIENT * value.getStepZ();
        }
    }
}
