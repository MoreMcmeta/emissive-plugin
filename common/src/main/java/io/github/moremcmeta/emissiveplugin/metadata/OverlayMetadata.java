package io.github.moremcmeta.emissiveplugin.metadata;

import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import net.minecraft.resources.ResourceLocation;

import static java.util.Objects.requireNonNull;

/**
 * Contains configuration data for texture overlays as read by the {@link OverlayMetadataAnalyzer}.
 * @author soir20
 */
public class OverlayMetadata implements AnalyzedMetadata {
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
