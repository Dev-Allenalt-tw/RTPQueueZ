package dev.allenalt.rtpqueuez.commands;

import dev.allenalt.rtpqueuez.RTPQueueZ;
import dev.allenalt.rtpqueuez.menu.QueueMenu;
import dev.allenalt.rtpqueuez.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPQueueCommand implements CommandExecutor {
    
    private final RTPQueueZ plugin;
    
    public RTPQueueCommand(RTPQueueZ plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        if (!player.hasPermission("rtpqueue.use")) {
            MessageUtils.sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            QueueMenu.openMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "status" -> {
                if (plugin.getQueueManager().isInQueue(player)) {
                    player.sendMessage(ChatColor.GREEN + "You are currently in queue!");
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in queue!");
                }
            }
            case "leave" -> {
                if (plugin.getQueueManager().leaveQueue(player)) {
                    MessageUtils.sendMessage(player, "queue-leaved");
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in queue!");
                }
            }
            case "overworld", "nether", "end" -> {
                String worldType = args[0].toLowerCase();
                
                if (!plugin.getConfig().getBoolean(worldType + ".enable", true)) {
                    player.sendMessage(ChatColor.RED + "This world type is disabled!");
                    return true;
                }
                
                if (plugin.getQueueManager().joinQueue(player, worldType)) {
                    MessageUtils.sendMessage(player, "queue-joined");
                } else {
                    player.sendMessage(ChatColor.RED + "You are already in queue or on cooldown!");
                }
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Usage: /rtpqueue [overworld|nether|end|status|leave]");
            }
        }
        
        return true;
    }
}
