package de.leo.kakaoEssentials;

import de.leo.kakaoEssentials.util.PlayerUtil;
import de.leo.kakaoEssentials.util.WorldguardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventHandler implements Listener {
    private static EventHandler INSTANCE;
    private WorldguardUtil worldguardUtil;
    private KakaoEssentials kakaoEssentials;

    private int cooldownMillis;

    private EventHandler() {
        // test
    }

    public static EventHandler getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new EventHandler();
        }
        return INSTANCE;
    }

    public void init(KakaoEssentials kakaoEssentials){
        Bukkit.getPluginManager().registerEvents(this, kakaoEssentials);
        this.kakaoEssentials = kakaoEssentials;
        worldguardUtil = WorldguardUtil.getInstance();

        cooldownMillis = kakaoEssentials.getConfig().getInt("movement-cooldown", 1000);
    }

    @org.bukkit.event.EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerUtil.clearPlayerInventory(event.getPlayer());
        PlayerUtil.removeEffects(event.getPlayer());
    }

    @org.bukkit.event.EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String name = ChatColor.stripColor(meta.getDisplayName());

        if ("Death Stick".equalsIgnoreCase(name)) {
            event.setCancelled(true); // cancel to override with kill
            victim.setHealth(0);
        } else if ("Damage Stick".equalsIgnoreCase(name)) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            Double damage = container.get(new NamespacedKey(kakaoEssentials, "damage_amount"), PersistentDataType.DOUBLE);
            Double kb = container.get(new NamespacedKey(kakaoEssentials, "knockback_power"), PersistentDataType.DOUBLE);

            if (damage != null) {
                event.setCancelled(true);
                victim.damage(damage); // ✅ Let Minecraft apply it, so KB & animation works
                Vector direction = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                direction.setY(0.4); // upward boost
                direction.multiply(kb); // tweak strength

                victim.setVelocity(direction);
            }
        }
    }

    @org.bukkit.event.EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Delay check by 1 tick to allow correct location update
        new BukkitRunnable() {
            @Override
            public void run() {
                worldguardUtil.handleRegionChange(player);
            }
        }.runTask(kakaoEssentials);
    }

    public void configReloaded(){
        cooldownMillis = kakaoEssentials.getConfig().getInt("movement-cooldown", 1000);
    }

    private final Map<UUID, Long> lastMoveCheck = new HashMap<>();

    @org.bukkit.event.EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Not a meaningful movement
        }

        UUID playerId = event.getPlayer().getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check cooldown
        if (lastMoveCheck.containsKey(playerId)) {
            long lastCheck = lastMoveCheck.get(playerId);
            if (currentTime - lastCheck < cooldownMillis) {
                return; // Still in cooldown
            }
        }

        lastMoveCheck.put(playerId, currentTime);
        worldguardUtil.handleRegionChange(event.getPlayer());
    }
}
