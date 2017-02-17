package com.winthier.custom_tnt;

import com.winthier.custom.event.CustomRegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomTNTPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCustomRegister(CustomRegisterEvent event) {
        event.addItem(new CustomTNTItem(this, "tnt"));
        event.addBlock(new CustomTNTBlock(this, "tnt"));
        event.addEntity(new CustomTNTEntity(this, "tnt"));
    }
}
