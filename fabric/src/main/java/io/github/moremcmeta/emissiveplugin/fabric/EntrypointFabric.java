package io.github.moremcmeta.emissiveplugin.fabric;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class EntrypointFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register(
                (atlas, registry) -> ModConstants.SPRITE_REGISTRAR_CONSUMER.accept(registry::register)
        );
    }
}
