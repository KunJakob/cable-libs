package com.cable.library;

import com.cable.library.tasks.TaskTickListener;
import com.cable.library.text.ClickTextCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(
        modid = CableLibs.MOD_ID,
        name = CableLibs.MOD_NAME,
        version = CableLibs.VERSION,
        acceptableRemoteVersions = "*"
)
public class CableLibs {

    public static final String MOD_ID = "cable-libs";
    public static final String MOD_NAME = "CableLibs";
    public static final String VERSION = "1.0";

    /** This is the instance of your mod as created by Forge. It will never be null. */
    @Mod.Instance(MOD_ID)
    public static CableLibs INSTANCE;

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ClickTextCommand());
    }

}
