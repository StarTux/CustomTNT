package com.winthier.custom_tnt;

import com.winthier.custom.event.CustomRegisterEvent;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CustomTNTPlugin extends JavaPlugin implements Listener {
    private final Map<CustomTNTType, CustomTNTItem> items = new EnumMap<>(CustomTNTType.class);
    private final Map<CustomTNTType, CustomTNTBlock> blocks = new EnumMap<>(CustomTNTType.class);
    private final Map<CustomTNTType, CustomTNTEntity> entities = new EnumMap<>(CustomTNTType.class);
    private BombBagItem bombBagItem;
    private BombBagBlock bombBagBlock;

    @Override
    public void onEnable() {
        // saveDefaultConfig(); TODO uncomment!!!
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCustomRegister(CustomRegisterEvent event) {
        reloadConfig();
        for (CustomTNTType type: CustomTNTType.values()) {
            CustomTNTItem item = new CustomTNTItem(this, type);
            CustomTNTBlock block = new CustomTNTBlock(this, type);
            CustomTNTEntity entity = new CustomTNTEntity(this, type);
            items.put(type, item);
            blocks.put(type, block);
            entities.put(type, entity);
            event.addItem(item);
            event.addBlock(block);
            event.addEntity(entity);
        }
        this.bombBagItem = new BombBagItem(this);
        this.bombBagBlock = new BombBagBlock(this);
        event.addItem(bombBagItem);
        event.addBlock(bombBagBlock);
    }
}
