package co.aikar.commands;

import com.cable.library.CableLibs;

import java.util.Locale;

public class ForgeLocales extends Locales {
    private final ForgeCommandManager manager;

    public ForgeLocales(ForgeCommandManager manager) {
        super(manager);
        this.manager = manager;
        this.addBundleClassLoader(this.manager.getMod().getClass().getClassLoader());
    }

    @Override
    public void loadLanguages() {
        super.loadLanguages();
        String pluginName = "acf-" + CableLibs.MOD_ID;
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase(Locale.ENGLISH));
    }
}
