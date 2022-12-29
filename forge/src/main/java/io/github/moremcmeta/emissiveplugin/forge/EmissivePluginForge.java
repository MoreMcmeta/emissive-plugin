package io.github.moremcmeta.emissiveplugin.forge;

import io.github.moremcmeta.emissiveplugin.ModConstants;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(ModConstants.MOD_ID)
public class EmissivePluginForge {

    /**
     * Serves as mod entrypoint on Forge and tells the server to ignore this mod.
     */
    public EmissivePluginForge() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                ()-> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer)-> true
                )
        );
    }

}
