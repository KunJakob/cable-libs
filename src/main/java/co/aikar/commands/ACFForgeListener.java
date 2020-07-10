package co.aikar.commands;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.lang.reflect.Field;
import java.util.Locale;

@Mod.EventBusSubscriber
public class ACFForgeListener {

    private final ForgeCommandManager manager;

    public ACFForgeListener(ForgeCommandManager manager) {
        this.manager = manager;
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        //this event will be fired on join as well as every time the player changes it
        try {
            manager.setIssuerLocale(event.player, Locale.forLanguageTag((String) langField.get(event.player)));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    @SubscribeEvent
    public void onDisconnectCleanup(PlayerEvent.PlayerLoggedOutEvent event) {
        manager.issuersLocale.remove(event.player.getUniqueID());
    }

    private static Field langField;

    static {
        try {
            langField = EntityPlayerMP.class.getDeclaredField("language");
            langField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
