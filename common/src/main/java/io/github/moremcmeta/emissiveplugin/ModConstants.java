package io.github.moremcmeta.emissiveplugin;

import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;

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
}
