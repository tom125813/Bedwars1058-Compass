package club.mher.compass.listener;

import club.mher.compass.Compass;
import club.mher.compass.data.MainConfig;
import club.mher.compass.menu.menus.TrackerMenu;
import club.mher.compass.util.NBTItem;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class QuickBuyListener implements Listener {

    @EventHandler
    public void onShop(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        if (!isShop(player, e.getView().getTitle())) {
            return;
        }
        NBTItem item = new NBTItem(Compass.getMainConfig().getItem(player, MainConfig.TRACKER_SHOP, true, "tracker-shop"));
        e.getInventory().setItem(item.getInteger("slot"), item.getItem());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!Compass.getBedWars().getArenaUtil().isPlaying(player)) {
            return;
        }
        if (!isShop(player, e.getView().getTitle())) {
            return;
        }
        ItemStack is = e.getCurrentItem();
        if (is == null || is.getType() == Material.AIR) return;
        String data = new NBTItem(is).getString("data");
        if (data == null || !data.equals("tracker-shop")) {
            return;
        }
        IArena a = Compass.getBedWars().getArenaUtil().getArenaByPlayer(player);
        TrackerMenu tm = new TrackerMenu(player, a);
        tm.setBackToShop(true);
        tm.open();
    }

    private boolean isShop(Player player, String title) {
        return title.equalsIgnoreCase(Language.getMsg(player, Messages.SHOP_INDEX_NAME));
    }

    @EventHandler
    public void onClicke(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();

        // Ensure clicked item is not null and is a compass
        if (clickedItem != null && clickedItem.getType() == Material.COMPASS) {

            // If compass is in the hotbar (Slots 0-8)
            if (event.getSlot() >= 0 && event.getSlot() <= 8) {
                // Find the next available slot in the top rows of the inventory
                for(int i = 9; i <= 35; i++){
                    if(inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        inventory.setItem(i, clickedItem);
                        event.getWhoClicked().getInventory().clear(event.getSlot());
                        event.setCancelled(true);
                        break;
                    }
                }
            }

            // If compass is in the top rows of the inventory (Slots 9-35)
            else if (event.getSlot() >= 9 && event.getSlot() <= 35) {
                // Find the next available slot in the hotbar
                for(int i = 8; i >= 0; i--){
                    if(inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        inventory.setItem(i, clickedItem);
                        event.getWhoClicked().getInventory().clear(event.getSlot());
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();
        ItemStack itemStack = droppedItem.getItemStack();
        Inventory inventory = event.getPlayer().getInventory();

        // Ensure dropped item is a compass
        if (itemStack.getType() == Material.COMPASS) {

            // If the compass was dropped from the hotbar (Slots 0-8)
            int heldItemSlot = event.getPlayer().getInventory().getHeldItemSlot();
            if (heldItemSlot >= 0 && heldItemSlot <= 8) {

                // Find the next available slot in the top rows of the inventory
                for (int i = 9; i <= 35; i++) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        inventory.setItem(i, itemStack);
                        Bukkit.getServer().getScheduler().runTaskLater(Compass.getInstance(), ()->{inventory.clear(heldItemSlot);},1L); // Clear the slot from where the compass was dropped
                        droppedItem.remove(); // Remove the dropped item entity
                        event.setCancelled(true);
                        break;
                    }
                }
            }

            // If the compass was dropped from the top rows of the inventory (Slots 9-35)
            else if (heldItemSlot >= 9 && heldItemSlot <= 35) {

                // Find the next available slot in the hotbar
                for (int i = 8; i >= 0; i--) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        inventory.setItem(i, itemStack);
                        Bukkit.getServer().getScheduler().runTaskLater(Compass.getInstance(), ()->{inventory.clear(heldItemSlot);},1L); // Clear the slot from where the compass was dropped
                        droppedItem.remove(); // Remove the dropped item entity
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }


}