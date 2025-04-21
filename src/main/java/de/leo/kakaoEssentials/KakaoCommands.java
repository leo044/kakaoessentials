package de.leo.kakaoEssentials;

import de.leo.kakaoEssentials.util.SaveAndLoad;
import de.leo.kakaoEssentials.util.WorldguardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KakaoCommands {
    private static KakaoEssentials kakaoEssentials;
    public static void registerCommands(KakaoEssentials main){
        kakaoEssentials = main;
        SaveAndLoad saveAndLoad = SaveAndLoad.getInstance();
        main.getCommand("saveitems").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;

            if (!player.hasPermission("kakao.saveitems")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            String region = WorldguardUtil.getInstance().getRegionName(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "You are not in a WorldGuard region.");
                return true;
            }

            ItemStack[] contents = player.getInventory().getContents();
            main.regionItemMap.put(region, Arrays.copyOf(contents, contents.length));
            saveAndLoad.saveRegionItems(region, contents);
            player.sendMessage(ChatColor.GREEN + "Items saved for region: " + region);
            return true;
        });

        main.getCommand("loaditems").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;

            if (!player.hasPermission("kakao.loaditems")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /loaditems <region>");
                return true;
            }

            String region = args[0];

            if (!main.regionItemMap.containsKey(region)) {
                player.sendMessage(ChatColor.RED + "No items saved for region: " + region);
                return true;
            }

            player.getInventory().clear();
            player.getInventory().setContents(Arrays.copyOf(main.regionItemMap.get(region), main.regionItemMap.get(region).length));
            player.sendMessage(ChatColor.GREEN + "Items loaded for region: " + region);
            return true;
        });

        main.getCommand("instastick").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("kakao.instastick")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }


            ItemStack stick = new ItemStack(org.bukkit.Material.STICK);
            org.bukkit.inventory.meta.ItemMeta meta = stick.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_RED + "Death Stick");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Kills on hit, bypasses PvP."));
            stick.setItemMeta(meta);

            player.getInventory().addItem(stick);
            player.sendMessage(ChatColor.GREEN + "You received the Death Stick.");
            return true;
        });

        main.getCommand("damagestick").setExecutor(KakaoCommands::damageStickCommand);

        main.getCommand("setenter").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("kakao.setmessage")) {
                player.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /setenter <region> <message>");
                return true;
            }

            String region = args[0];
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            saveAndLoad.saveMessage(region, "enter", message);
            player.sendMessage(ChatColor.GREEN + "Enter message for region '" + region + "' set.");
            return true;
        });

        main.getCommand("setleave").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("kakao.setmessage")) {
                player.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /setleave <region> <message>");
                return true;
            }

            String region = args[0];
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            saveAndLoad.saveMessage(region, "leave", message);
            player.sendMessage(ChatColor.GREEN + "Leave message for region '" + region + "' set.");
            return true;
        });

        main.getCommand("reloadmessages").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("kakao.reload")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            saveAndLoad.loadMessages();
            sender.sendMessage(ChatColor.GREEN + "Region messages reloaded.");
            return true;
        });
        main.getCommand("w").setExecutor(KakaoCommands::wCommand);

        main.getCommand("reloadConfig").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("kakao.reload")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            main.reloadConfig();
            EventHandler.getInstance().configReloaded();
            sender.sendMessage("§a[KakaoEssentials] Config reloaded.");
            return true;
        });

        main.getCommand("skull").setExecutor(KakaoCommands::SkullCommand);
    }

    private static boolean SkullCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("kakao.damagestick")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <playerName>");
            return true;
        }

        String targetName = args[0];

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(targetName));
            meta.setDisplayName(ChatColor.GOLD + targetName + "'s Head");
            skull.setItemMeta(meta);
        }

        player.getInventory().addItem(skull);
        player.sendMessage(ChatColor.GREEN + "You received " + targetName + "'s head!");

        return true;
    }


    private static boolean damageStickCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("kakao.damagestick")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        double damage = 3.0;
        double knockback = 0.5;
        double height = 0.4; // default

        if (args.length >= 1) {
            try {
                damage = Double.parseDouble(args[0]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(ChatColor.RED + "Invalid damage value.");
            }
        }

        if (args.length >= 2) {
            try {
                knockback = Double.parseDouble(args[1]);
                if (knockback < -1000 || knockback > 1000) {
                    player.sendMessage(ChatColor.RED + "Knockback strength must be between -1000 and 1000.");
                    return true;
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(ChatColor.RED + "Invalid knockback value.");
                return true;
            }
        }

        if (args.length >= 3) {
            try {
                height = Double.parseDouble(args[2]);
                if (height < 0 || height > 10) {
                    player.sendMessage(ChatColor.RED + "Height multiplier must be between 0 and 10.");
                    return true;
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(ChatColor.RED + "Invalid height multiplier.");
                return true;
            }
        }

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Damage Stick");
        meta.setLore(List.of(
                ChatColor.GRAY + "Deals " + damage + " damage",
                ChatColor.GRAY + "Knockback: " + knockback + "x, Height: " + height + "x"
        ));

        NamespacedKey dmgKey = new NamespacedKey(kakaoEssentials, "damage_amount");
        NamespacedKey kbKey = new NamespacedKey(kakaoEssentials, "knockback_power");
        NamespacedKey heightKey = new NamespacedKey(kakaoEssentials, "knockback_height");

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(dmgKey, PersistentDataType.DOUBLE, damage);
        container.set(kbKey, PersistentDataType.DOUBLE, knockback);
        container.set(heightKey, PersistentDataType.DOUBLE, height);

        stick.setItemMeta(meta);
        player.getInventory().addItem(stick);
        player.sendMessage(ChatColor.GREEN + "You received a Damage Stick.");
        return true;
    }



    private static boolean wCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!sender.hasPermission("kakao.commandwrapper")){
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            return true;
        }

        Player player = (Player) sender;
        String inputArg = args[0];
        List<String> commands = kakaoEssentials.getConfig().getStringList("wrapped-commands");

        for (String cmd : commands) {
            String parsed = cmd
                    .replace("%arg%", inputArg);
            if (parsed.startsWith("/")){
                parsed = parsed.substring(1);
                kakaoEssentials.getServer().dispatchCommand(player, parsed);
            } else{
                player.chat(parsed);
            }
        }

        player.sendMessage("§aExecuted wrapped commands.");
        return true;
    }
}
