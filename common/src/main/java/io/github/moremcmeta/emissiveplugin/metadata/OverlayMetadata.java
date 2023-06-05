package io.github.moremcmeta.emissiveplugin.metadata;

import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import net.minecraft.resources.ResourceLocation;

import static java.util.Objects.requireNonNull;

public class OverlayMetadata implements AnalyzedMetadata {
    private final ResourceLocation OVERLAY_LOCATION;
    private final boolean IS_EMISSIVE;

    public OverlayMetadata(ResourceLocation overlayLocation, boolean isEmissive) {
        OVERLAY_LOCATION = requireNonNull(overlayLocation, "Overlay location cannot be null");
        IS_EMISSIVE = isEmissive;
    }

    public ResourceLocation overlayLocation() {
        return OVERLAY_LOCATION;
    }

    public boolean isEmissive() {
        return IS_EMISSIVE;
    }
}
