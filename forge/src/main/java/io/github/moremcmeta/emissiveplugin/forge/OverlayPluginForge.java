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

package io.github.moremcmeta.emissiveplugin.forge;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.forge.api.client.MoreMcmetaClientPlugin;

/**
 * Implementation of the overlay plugin on Forge.
 * @author soir20
 */
@SuppressWarnings("unused")
@MoreMcmetaClientPlugin
public final class OverlayPluginForge implements MoreMcmetaTexturePlugin {
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
