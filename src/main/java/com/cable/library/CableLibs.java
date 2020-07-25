package com.cable.library;

import com.cable.library.text.ClickTextCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

@Mod(
        modid = CableLibs.MOD_ID,
        name = CableLibs.MOD_NAME,
        version = CableLibs.VERSION,
        acceptableRemoteVersions = "*"
)
public class CableLibs {

    public static final String MOD_ID = "cable-libs";
    public static final String MOD_NAME = "CableLibs";
    public static final String VERSION = "1.1";

    /** This is the instance of your mod as created by Forge. It will never be null. */
    @Mod.Instance(MOD_ID)
    public static CableLibs INSTANCE;

    private Logger logger = Logger.getLogger("Cable-Libs");

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ClickTextCommand());
    }

    public Logger getLogger() {
        return logger;
    }

    public static void logError(String error) {
        INSTANCE.logger.log(Level.SEVERE, error);
    }

    public static void logWarn(String warn) {
        INSTANCE.logger.log(Level.WARNING, warn);
    }

    public static void logInfo(String info) {
        INSTANCE.logger.log(Level.INFO, info);
    }
}
