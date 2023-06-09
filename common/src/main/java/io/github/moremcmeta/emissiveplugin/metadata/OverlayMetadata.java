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

package io.github.moremcmeta.emissiveplugin.metadata;

import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import net.minecraft.resources.ResourceLocation;

import static java.util.Objects.requireNonNull;

/**
 * Contains configuration data for texture overlays as read by the {@link OverlayMetadataAnalyzer}.
 * @author soir20
 */
public final class OverlayMetadata implements AnalyzedMetadata {
    private final ResourceLocation OVERLAY_SPRITE_NAME;
    private final boolean IS_EMISSIVE;

    /**
     * Creates overlay metadata.
     * @param overlaySpriteName     sprite name of the overlay texture
     * @param isEmissive            whether the overlay should be emissive
     */
    public OverlayMetadata(ResourceLocation overlaySpriteName, boolean isEmissive) {
        OVERLAY_SPRITE_NAME = requireNonNull(overlaySpriteName, "Overlay sprite name cannot be null");
        IS_EMISSIVE = isEmissive;
    }

    /**
     * Gets the sprite name of the overlay texture.
     * @return sprite name of the overlay texture
     */
    public ResourceLocation overlaySpriteName() {
        return OVERLAY_SPRITE_NAME;
    }

    /**
     * Gets whether the overlay should be emissive.
     * @return whether the overlay should be emissive
     */
    public boolean isEmissive() {
        return IS_EMISSIVE;
    }

}
