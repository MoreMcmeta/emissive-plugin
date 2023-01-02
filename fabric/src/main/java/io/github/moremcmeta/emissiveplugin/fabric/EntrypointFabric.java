package io.github.moremcmeta.emissiveplugin.fabric;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.OverlayMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class EntrypointFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register(
                (atlas, registry) ->
                        MetadataRegistry.INSTANCE.metadataByPlugin(ModConstants.DISPLAY_NAME).values().forEach(
                                (metadata) -> registry.register(((OverlayMetadata) metadata).overlayLocation())
                        )
        );
    }
}
