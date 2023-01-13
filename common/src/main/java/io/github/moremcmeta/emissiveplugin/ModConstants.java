package io.github.moremcmeta.emissiveplugin;

import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadata;
import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

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
    public static final Consumer<Consumer<ResourceLocation>> SPRITE_REGISTRAR_CONSUMER = (registrar) ->
            MetadataRegistry.INSTANCE.metadataByPlugin(ModConstants.DISPLAY_NAME).values().forEach(
                    (metadata) -> registrar.accept(((OverlayMetadata) metadata).overlayLocation())
            );
}
