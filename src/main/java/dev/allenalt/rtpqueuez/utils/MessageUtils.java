package dev.allenalt.rtpqueuez.utils;

import dev.allenalt.rtpqueuez.RTPQueueZ;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class MessageUtils {
    
    private static FileConfiguration messagesConfig;
    
    public static void loadMessages() {
        File messagesFile = new File(RTPQueueZ.getInstance().getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    private static FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            loadMessages();
        }
        return messagesConfig;
    }
    
    public static void sendMessage(Player player, String path) {
        List<String> messages = getMessagesConfig().getStringList(path);
        String prefix = RTPQueueZ.getInstance().getConfig().getString("prefix", "&aRTPQueueZ &7» ");
        
        for (String message : messages) {
            message = message.replace("%prefix%", prefix);
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }
    }
    
    public static void sendMessageWithPlaceholder(Player player, String path, String placeholder, String value) {
        List<String> messages = getMessagesConfig().getStringList(path);
        String prefix = RTPQueueZ.getInstance().getConfig().getString("prefix", "&aRTPQueueZ &7» ");
        
        for (String message : messages) {
            message = message.replace("%prefix%", prefix);
            message = message.replace(placeholder, value);
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }
    }
    
    public static void sendTitle(Player player, String path, String placeholder, String value) {
        String title = getMessagesConfig().getString(path, "");
        String subtitle = getMessagesConfig().getString(path, "");
        
        title = ChatColor.translateAlternateColorCodes('&', title.replace(placeholder, value));
        subtitle = ChatColor.translateAlternateColorCodes('&', subtitle.replace(placeholder, value));
        
        player.sendTitle(title, subtitle, 10, 40, 10);
    }
}
