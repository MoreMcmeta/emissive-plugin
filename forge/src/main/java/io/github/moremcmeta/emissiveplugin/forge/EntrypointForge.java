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

package io.github.moremcmeta.emissiveplugin.forge;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod entrypoint on Forge.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mod(ModConstants.MOD_ID)
public final class EntrypointForge {

    /**
     * Serves as mod entrypoint on Forge and tells the server to ignore this mod.
     */
    public EntrypointForge() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(
                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer) -> true
                )
        );
    }

}
