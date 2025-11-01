package dev.allenalt.rtpqueuez.menu;

import dev.allenalt.rtpqueuez.RTPQueueZ;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class QueueMenu implements Listener {
    
    private static final String MENU_TITLE = ChatColor.translateAlternateColorCodes('&', "&aRTPQueueZ Menu");
    
    public static void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);
        
        RTPQueueZ plugin = RTPQueueZ.getInstance();
        
        addMenuItem(inv, player, "overworld", plugin);
        addMenuItem(inv, player, "nether", plugin);
        addMenuItem(inv, player, "end", plugin);
        
        player.openInventory(inv);
    }
    
    private static void addMenuItem(Inventory inv, Player player, String worldType, RTPQueueZ plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(worldType);
        
        if (section == null || !section.getBoolean("enable", false)) {
            return;
        }
        
        Material material = Material.valueOf(section.getString("material", "GRASS_BLOCK"));
        int slot = section.getInt("slot", 11);
        String displayName = ChatColor.translateAlternateColorCodes('&', section.getString("display_name", worldType));
        List<String> lore = section.getStringList("lore");
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            
            List<String> processedLore = new ArrayList<>();
            int queueCount = plugin.getQueueManager().getQueueCount(worldType);
            int maxPlayers = plugin.getConfig().getInt("max-players", 2);
            
            for (String line : lore) {
                line = ChatColor.translateAlternateColorCodes('&', line);
                line = line.replace("%rtpqueue_count_" + worldType + "%", String.valueOf(queueCount));
                line = line.replace("%max_players%", String.valueOf(maxPlayers));
                processedLore.add(line);
            }
            
            meta.setLore(processedLore);
            item.setItemMeta(meta);
        }
        
        inv.setItem(slot, item);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        if (!event.getView().getTitle().equals(MENU_TITLE)) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        RTPQueueZ plugin = RTPQueueZ.getInstance();
        int slot = event.getSlot();
        
        String worldType = getWorldTypeFromSlot(slot, plugin);
        if (worldType != null) {
            player.closeInventory();
            player.performCommand("rtpqueue " + worldType);
        }
    }
    
    private String getWorldTypeFromSlot(int slot, RTPQueueZ plugin) {
        if (plugin.getConfig().getInt("overworld.slot") == slot) {
            return "overworld";
        } else if (plugin.getConfig().getInt("nether.slot") == slot) {
            return "nether";
        } else if (plugin.getConfig().getInt("end.slot") == slot) {
            return "end";
        }
        return null;
    }
}
