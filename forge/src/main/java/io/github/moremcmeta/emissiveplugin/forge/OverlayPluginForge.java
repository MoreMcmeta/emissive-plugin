package io.github.moremcmeta.emissiveplugin.forge;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;
import io.github.moremcmeta.moremcmeta.forge.api.client.MoreMcmetaClientPlugin;

/**
 * Implementation of the overlay plugin on Forge.
 * @author soir20
 */
@SuppressWarnings("unused")
@MoreMcmetaClientPlugin
public class OverlayPluginForge implements MoreMcmetaTexturePlugin {
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
