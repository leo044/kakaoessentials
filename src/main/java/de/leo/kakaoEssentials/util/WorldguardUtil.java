package de.leo.kakaoEssentials.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.leo.kakaoEssentials.KakaoEssentials;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class WorldguardUtil {
    private final Map<UUID, String> playerRegionMap = new HashMap<>();

    private static WorldguardUtil INSTANCE;

    public KakaoEssentials kakaoEssentials;

    private WorldguardUtil() {
    }

    public static WorldguardUtil getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new WorldguardUtil();
        }

        return INSTANCE;
    }

    public String getRegionName(Player player) {
        World world = BukkitAdapter.adapt(player.getWorld());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(world);
        if (regions == null) return null;

        com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(player.getLocation());
        BlockVector3 vector = weLoc.toVector().toBlockPoint();
        ApplicableRegionSet regionSet = regions.getApplicableRegions(vector);
        for (ProtectedRegion region : regionSet) {
            return region.getId(); // return the first matched region
        }
        return null;
    }

    public void handleRegionChange(Player player){
        String currentRegion = getRegionName(player);
        String lastRegion = playerRegionMap.get(player.getUniqueId());

        if (!Objects.equals(currentRegion, lastRegion)) {
            if (lastRegion != null) {
                if (kakaoEssentials.regionItemMap.containsKey(lastRegion)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    PlayerUtil.removeEffects(player);
                    PlayerUtil.clearPlayerInventory(player);
                }
                if (kakaoEssentials.leaveMessages.containsKey(lastRegion)) {
                    player.sendMessage(kakaoEssentials.leaveMessages.get(lastRegion));
                }
            }

            if (currentRegion != null) {
                if (kakaoEssentials.regionItemMap.containsKey(currentRegion)) {
                    PlayerUtil.clearPlayerInventory(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    PlayerUtil.giveItems(player, kakaoEssentials.regionItemMap.get(currentRegion));
                }
                if (kakaoEssentials.enterMessages.containsKey(currentRegion)) {
                    player.sendMessage(kakaoEssentials.enterMessages.get(currentRegion));
                }
            }

            playerRegionMap.put(player.getUniqueId(), currentRegion);
        }
    }
}
