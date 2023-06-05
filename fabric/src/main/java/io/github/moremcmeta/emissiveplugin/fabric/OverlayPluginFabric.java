package io.github.moremcmeta.emissiveplugin.fabric;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;

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
    public MetadataAnalyzer analyzer() {
        return ModConstants.ANALYZER;
    }

    @Override
    public ComponentBuilder componentBuilder() {
        return ModConstants.COMPONENT_BUILDER;
    }

    @Override
    public String id() {
        return ModConstants.MOD_ID;
    }
}