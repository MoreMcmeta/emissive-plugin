package io.github.moremcmeta.emissiveplugin.forge.event;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import io.github.moremcmeta.emissiveplugin.forge.model.OverlayBakedItemModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBake(ModelBakeEvent event) {
        Iterable<ModelResourceLocation> itemModels = ForgeRegistries.ITEMS.getKeys().stream().map(
                (location) -> new ModelResourceLocation(location, "inventory")
        ).toList();

        itemModels.forEach((location) ->
                event.getModelRegistry().computeIfPresent(
                        location,
                        (modelLocation, model) -> new OverlayBakedItemModel(model)
                )
        );
    }
}
