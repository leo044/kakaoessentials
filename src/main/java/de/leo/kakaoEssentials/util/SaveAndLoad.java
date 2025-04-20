package de.leo.kakaoEssentials.util;

import de.leo.kakaoEssentials.KakaoEssentials;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SaveAndLoad {
    private static SaveAndLoad INSTANCE;

    private FileConfiguration dataConfig;
    private File dataFile;
    private File messageFile;
    private FileConfiguration messageConfig;

    public KakaoEssentials kakaoEssentials;

    private SaveAndLoad() {
    }

    public static SaveAndLoad getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new SaveAndLoad();
        }

        return INSTANCE;
    }


    public void saveRegionItems(String region, ItemStack[] items) {
        dataConfig.set(region + ".items", items);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDataFile(File dataFolder) {
        dataFile = new File(dataFolder, "items.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveMessage(String region, String type, String message) {
        messageConfig.set(region + "." + type, message);
        try {
            messageConfig.save(messageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String colored = ChatColor.translateAlternateColorCodes('&', message);
        if (type.equals("enter")) {
            kakaoEssentials.enterMessages.put(region, colored);
        } else if (type.equals("leave")) {
            kakaoEssentials.leaveMessages.put(region, colored);
        }
    }

    public void createMessageFile(File dataFolder) {
        messageFile = new File(dataFolder, "messages.yml");
        if (!messageFile.exists()) {
            messageFile.getParentFile().mkdirs();
            try {
                messageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
    }

    public void loadMessages() {
        for (String region : messageConfig.getKeys(false)) {
            String enter = messageConfig.getString(region + ".enter");
            String leave = messageConfig.getString(region + ".leave");

            if (enter != null) kakaoEssentials.enterMessages.put(region, ChatColor.translateAlternateColorCodes('&', enter));
            if (leave != null) kakaoEssentials.leaveMessages.put(region, ChatColor.translateAlternateColorCodes('&', leave));
        }
    }

    public void loadSavedItems() {
        for (String region : dataConfig.getKeys(false)) {
            List<?> itemList = dataConfig.getList(region + ".items");
            if (itemList != null) {
                ItemStack[] contents = itemList.toArray(new ItemStack[0]);
                kakaoEssentials.regionItemMap.put(region, contents);
            }

            String enter = dataConfig.getString(region + ".enterMessage");
            String leave = dataConfig.getString(region + ".leaveMessage");

            if (enter != null) kakaoEssentials.enterMessages.put(region, ChatColor.translateAlternateColorCodes('&', enter));
            if (leave != null) kakaoEssentials.leaveMessages.put(region, ChatColor.translateAlternateColorCodes('&', leave));
        }
    }

}
