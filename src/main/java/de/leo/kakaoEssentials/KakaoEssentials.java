package de.leo.kakaoEssentials;

import de.leo.kakaoEssentials.util.SaveAndLoad;
import de.leo.kakaoEssentials.util.WorldguardUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class KakaoEssentials extends JavaPlugin {

    public final Map<String, ItemStack[]> regionItemMap = new HashMap<>();
    public final Map<String, String> enterMessages = new HashMap<>();
    public final Map<String, String> leaveMessages = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        SaveAndLoad saveAndLoad = SaveAndLoad.getInstance();
        WorldguardUtil worldguardUtil = WorldguardUtil.getInstance();
        worldguardUtil.kakaoEssentials = this;
        saveAndLoad.kakaoEssentials = this;

        saveAndLoad.createDataFile(getDataFolder());
        saveAndLoad.loadSavedItems();
        saveAndLoad.createMessageFile(getDataFolder());
        saveAndLoad.loadMessages();

        KakaoCommands.registerCommands(this);
        EventHandler eventHandler = EventHandler.getInstance();
        eventHandler.init(this);
    }
}
