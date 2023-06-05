package io.github.moremcmeta.emissiveplugin.metadata;

import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Parses overlay metadata into {@link OverlayMetadata}.
 * @author soir20
 */
public class OverlayMetadataAnalyzer implements MetadataAnalyzer {
    @Override
    public AnalyzedMetadata analyze(MetadataView metadata, int imageWidth, int imageHeight) throws InvalidMetadataException {
        requireNonNull(metadata, "Metadata cannot be null");

        Optional<String> rawOverlayLocation = metadata.stringValue("texture");
        if (rawOverlayLocation.isEmpty()) {
            throw new InvalidMetadataException("Overlays must have a texture defined");
        }

        ResourceLocation overlayLocation = ResourceLocation.tryParse(rawOverlayLocation.get());
        if (overlayLocation == null) {
            throw new InvalidMetadataException("Non [a-z0-9_.-] character in overlay texture location");
        }

        boolean isEmissive = metadata.booleanValue("emissive").orElse(false);

        return new OverlayMetadata(SpriteName.fromTexturePath(overlayLocation), isEmissive);
    }

}
