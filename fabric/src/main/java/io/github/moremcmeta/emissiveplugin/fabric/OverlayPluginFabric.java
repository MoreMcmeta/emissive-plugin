package io.github.moremcmeta.emissiveplugin.fabric;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;

/**
 * Implementation of the overlay plugin on Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public class OverlayPluginFabric implements MoreMcmetaTexturePlugin {
    @Override
    public String sectionName() {
        return ModConstants.SECTION_NAME;
    }

    @Override
    public MetadataParser parser() {
        return ModConstants.PARSER;
    }

    @Override
    public ComponentProvider componentProvider() {
        return ModConstants.COMPONENT_PROVIDER;
    }

    @Override
    public String displayName() {
        return ModConstants.DISPLAY_NAME;
    }

    @Override
    public boolean allowTextureAndSectionInDifferentPacks() {
        return true;
    }
}