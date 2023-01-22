package io.github.moremcmeta.emissiveplugin;

import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Constants for both Fabric and Forge implementations of the plugin.
 * @author soir20
 */
public class ModConstants {
    public static final String MOD_ID = "moremcmeta_emissive_plugin";
    public static final String SECTION_NAME = "overlay";
    public static final String DISPLAY_NAME = "MoreMcmeta Emissive Textures";
    public static final MetadataParser PARSER = new OverlayMetadataParser();
    public static final ComponentProvider COMPONENT_PROVIDER = (metadata, frames) -> new TextureComponent<>() {};
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

        MetadataRegistry.INSTANCE.metadataByPlugin(ModConstants.DISPLAY_NAME).forEach(
                (textureLocation, metadata) -> {
                    if (spriteTextures.contains(SpriteName.fromTexturePath(textureLocation))) {
                        sprites.add(new Material(
                                TextureAtlas.LOCATION_BLOCKS,
                                ((OverlayMetadata) metadata).overlayLocation()
                        ));
                    }
                }
        );
    };
    public static final ToBooleanBiFunction<ModelBakery, UnbakedModel> USES_OVERLAY = (bakery, original) -> {
        Set<Pair<String, String>> missingTextures = new HashSet<>();
        return original.getMaterials(bakery::getModel, missingTextures)
                .stream()
                .map(Material::texture)
                .anyMatch((spriteName) -> MetadataRegistry.INSTANCE.metadataFromSpriteName(
                        ModConstants.DISPLAY_NAME,
                        spriteName
                ).isPresent());
    };
}
