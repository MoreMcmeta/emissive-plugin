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

import io.github.moremcmeta.emissiveplugin.metadata.OverlayMetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import net.minecraft.client.resources.model.Material;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Constants for both Fabric and Forge implementations of the plugin.
 * @author soir20
 */
public final class ModConstants {
    public static final String MOD_ID = "moremcmeta_emissive_plugin";
    public static final String SECTION_NAME = "overlay";
    public static final MetadataAnalyzer ANALYZER = new OverlayMetadataAnalyzer();
    public static final ComponentBuilder COMPONENT_BUILDER = (metadata, frames) -> new TextureComponent<>() {};
    public static final Predicate<Set<Material>> USES_OVERLAY = (usedMaterials) -> usedMaterials.stream()
            .map(Material::texture)
            .anyMatch(
                    (spriteName) -> MetadataRegistry.INSTANCE.metadataFromSpriteName(
                            ModConstants.MOD_ID,
                            spriteName
                    ).isPresent()
            );
}
