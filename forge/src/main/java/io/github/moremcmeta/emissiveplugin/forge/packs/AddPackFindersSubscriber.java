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

package io.github.moremcmeta.emissiveplugin.forge.packs;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds this mod's custom resource packs.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AddPackFindersSubscriber {

    /**
     * Adds this mod's custom resource packs.
     * @param event     event to use to add the packs
     */
    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        String displayName = ModConstants.Z_FIGHTING_PACK_ID.getNamespace() + "/" + ModConstants.Z_FIGHTING_PACK_ID.getPath();
        event.addRepositorySource((consumer, constructor) -> consumer.accept(new Pack(
                ModConstants.Z_FIGHTING_PACK_ID.toString(),
                false,
                () -> new NamespaceRemappingPack(
                        displayName,
                        ModConstants.MOD_ID,
                        "minecraft",
                        Minecraft.getInstance().getResourceManager()
                ),
                Component.literal(displayName),
                Component.literal("Adjusts models to prevent overlay z-fighting"),
                PackCompatibility.COMPATIBLE,
                Pack.Position.TOP,
                false,
                PackSource.BUILT_IN,
                false
        )));
    }

}
