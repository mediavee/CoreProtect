package net.coreprotect.spigot;

import net.coreprotect.config.ConfigHandler;

public class SpigotAdapter {

    public static SpigotAdapter ADAPTER;

    public static void loadAdapter() {
        if (ConfigHandler.isSpigot) {
            SpigotAdapter.ADAPTER = new SpigotHandler();
        }
        else {
            SpigotAdapter.ADAPTER = new SpigotAdapter();
        }
    }

}
