package io.github.moremcmeta.emissiveplugin;

import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ParsedMetadata;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Parses overlay metadata into {@link OverlayMetadata}.
 * @author soir20
 */
public class OverlayMetadataParser implements MetadataParser {
    @Override
    public ParsedMetadata parse(MetadataView metadata, int imageWidth, int imageHeight) throws InvalidMetadataException {
        requireNonNull(metadata, "Metadata cannot be null");

        MetadataView sectionMetadata = metadata.subView(ModConstants.SECTION_NAME).orElseThrow();
        Optional<String> rawOverlayLocation = sectionMetadata.stringValue("texture");
        if (rawOverlayLocation.isEmpty()) {
            throw new InvalidMetadataException("Overlays must have a texture defined");
        }

        ResourceLocation overlayLocationAsSprite = ResourceLocation.tryParse(rawOverlayLocation.get());
        if (overlayLocationAsSprite == null) {
            throw new InvalidMetadataException("Non [a-z0-9_.-] character in overlay texture location");
        }

        boolean isEmissive = sectionMetadata.booleanValue("emissive").orElse(false);

        return new OverlayMetadata(makeTextureLocation(overlayLocationAsSprite), isEmissive);
    }

    /**
     * Converts a sprite name to a standard texture location (with textures/ prefix and .png suffix).
     * @param location      the sprite name to convert
     * @return the texture location corresponding to the sprite
     */
    private static ResourceLocation makeTextureLocation(ResourceLocation location) {
        String originalPath = location.getPath();
        String fullPath = "textures/" + originalPath + ".png";
        return new ResourceLocation(location.getNamespace(), fullPath);
    }

}
