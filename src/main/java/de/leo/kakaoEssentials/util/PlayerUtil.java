package de.leo.kakaoEssentials.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;

public class PlayerUtil {
    public static void clearPlayerInventory(Player player) {
        player.closeInventory(); // Force cursor & crafting grid items back into inventory

        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setExtraContents(null); // Offhand, etc.
        player.setItemOnCursor(null); // Just in case, for good measure
        if (player.getOpenInventory().getType() == org.bukkit.event.inventory.InventoryType.CRAFTING) {
            for (int i = 1; i <= 4; i++) { // Slots 1-4 are the 2x2 grid
                player.getOpenInventory().setItem(i, null);
            }
        }
    }

    public static void giveItems(Player player, ItemStack[] items) {
        PlayerInventory inv = player.getInventory();
        inv.setContents(Arrays.copyOf(items, items.length));
    }

    public static void removeEffects(Player p){
        for(PotionEffect e : p.getActivePotionEffects()){
            p.removePotionEffect(e.getType());
        }
    }
}
