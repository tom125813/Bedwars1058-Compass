package club.mher.compass.listener;

import club.mher.compass.Compass;
import club.mher.compass.data.MainConfig;
import club.mher.compass.util.NBTItem;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class GameListener implements Listener {

    @EventHandler
    public void onServerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Compass.getBedWars().getArenaUtil().isPlaying(player)) return;
        IArena arena = Compass.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Compass.isTracking(arena, uuid)) return;
        Compass.removeTrackingTeam(arena, uuid);
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent e) {
        IArena arena = e.getArena();
        UUID uuid = e.getPlayer().getUniqueId();
        if (Compass.isTracking(arena, uuid)) Compass.removeTrackingTeam(arena, uuid);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (!Compass.getBedWars().getArenaUtil().isPlaying(player)) return;
        NBTItem nbti = new NBTItem(Compass.getMainConfig().getItem(player, MainConfig.COMPASS_ITEM, true, "compass-item"));
        e.getDrops().remove(nbti.getItem());
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        IArena arena = e.getArena();
        Player victim = e.getVictim();
        UUID victimUniqueId = victim.getUniqueId();
        ITeam victimTeam = arena.getTeam(victim);
        if (Compass.isTracking(arena, victimUniqueId)) Compass.removeTrackingTeam(arena, victimUniqueId);
        if (victimTeam.getMembers().size() == 0) Compass.getTrackingArenaMap().values().removeIf(victimTeam::equals);
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent e) {
        addToInventory(e.getPlayer());
    }

    @EventHandler
    public void onStateChange(GameStateChangeEvent e) {
        IArena arena = e.getArena();
        if (e.getNewState().equals(GameState.playing)) {
            arena.getPlayers().forEach(this::addToInventory);
        } else if (e.getNewState().equals(GameState.restarting)) {
            Compass.removeTrackingArena(arena);
        }
    }

    /*@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isShiftClick()) {
            ItemStack is = event.getCurrentItem();
            String data = new NBTItem(is).getString("data");

            if (data != null && data.equals("compass-item")) {
                Player player = (Player) event.getWhoClicked();
                Inventory inv = player.getInventory();

                if (event.getSlot() > 8) {
                    int indexToMove = -1;
                    for (int i = 8; i >= 0; i--) { // Look from the end of the bar (slot 8) to the start
                        if (inv.getItem(i) == null) { // If there's space, that's our index
                            indexToMove = i;
                            break;
                        }
                    }

                    if (indexToMove != -1) {
                        event.setCancelled(true);
                        inv.setItem(indexToMove, is);
                        int finalIndexToMove = indexToMove;
                        Bukkit.getServer().getScheduler().runTaskLater(Compass.getInstance(), ()->{inv.getItem(finalIndexToMove).setAmount(1);},1L);
                        inv.clear(event.getSlot());
                    }
                }
            }
        }
    }*/

    public void addToInventory(Player p) {
        NBTItem nbti = new NBTItem(Compass.getMainConfig().getItem(p, MainConfig.COMPASS_ITEM, true, "compass-item"));

        if (!p.getInventory().contains(nbti.getItem()))
            p.getInventory().setItem(nbti.getInteger("slot"), nbti.getItem());
    }

}