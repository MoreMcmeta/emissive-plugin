package io.github.myorg.myplugin.forge;

import io.github.myorg.myplugin.ExampleMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(ExampleMod.MOD_ID)
public class ExampleModForge extends ExampleMod {

    /**
     * Serves as mod entrypoint on Forge and tells the server to ignore this mod.
     */
    public ExampleModForge() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                ()-> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer)-> true
                )
        );

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> this::start);
    }

}
