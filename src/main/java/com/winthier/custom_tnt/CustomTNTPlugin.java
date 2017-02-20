package com.winthier.custom_tnt;

import com.winthier.custom.event.CustomRegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomTNTPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCustomRegister(CustomRegisterEvent event) {
        reloadConfig();
        for (CustomTNTType type: CustomTNTType.values()) {
            event.addItem(new CustomTNTItem(this, type));
            event.addBlock(new CustomTNTBlock(this, type));
            event.addEntity(new CustomTNTEntity(this, type));
        }
    }
}
