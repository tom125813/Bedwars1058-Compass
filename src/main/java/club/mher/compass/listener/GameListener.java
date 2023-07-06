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

    @EventHandler
    public void onCompassDrop(ItemSpawnEvent e) {
        ItemStack is = e.getEntity().getItemStack();
        if (is == null) return;
        String data = new NBTItem(is).getString("data");
        if (data == null) return;
        if (data.equals("compass-item")) e.setCancelled(true);
    }

    /*@EventHandler
    public void onCompassDrop(PlayerDropItemEvent e) {
        ItemStack is = e.getItemDrop().getItemStack();
        if (is == null) return;
        String data = new NBTItem(is).getString("data");
        if (data == null) return;
        if (!data.equals("compass-item")) return;

        Player player = e.getPlayer();
        Inventory inv = player.getInventory();

        // Cancel the drop event
        e.setCancelled(true);

        // Clone the item
        //ItemStack isClone = is.clone();

        // Remove all similar items from the player's inventory
        //inv.remove(is);

        // Find first empty slot in the inventory
        //int firstEmptySlot = inv.firstEmpty();
        //if (firstEmptySlot != -1) {
        //    // Add the cloned item back into the player's inventory at the first available slot
        //    inv.setItem(firstEmptySlot, isClone);
        //}

        Bukkit.getServer().getScheduler().runTaskLater(Compass.getInstance(), () -> {
            player.closeInventory();
        }, 2L);
    }*/

    public void addToInventory(Player p) {
        NBTItem nbti = new NBTItem(Compass.getMainConfig().getItem(p, MainConfig.COMPASS_ITEM, true, "compass-item"));
        p.getInventory().setItem(nbti.getInteger("slot"), nbti.getItem());
    }

}