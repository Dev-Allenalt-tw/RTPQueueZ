package dev.allenalt.rtpqueuez;

import dev.allenalt.rtpqueuez.commands.RTPQueueCommand;
import dev.allenalt.rtpqueuez.placeholders.RTPQueuePlaceholders;
import dev.allenalt.rtpqueuez.queue.RTPQueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RTPQueueZ extends JavaPlugin {
    
    private static RTPQueueZ instance;
    private RTPQueueManager queueManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        saveResource("messages.yml", false);
        
        queueManager = new RTPQueueManager(this);
        
        getCommand("rtpqueue").setExecutor(new RTPQueueCommand(this));
        getServer().getPluginManager().registerEvents(new dev.allenalt.rtpqueuez.menu.QueueMenu(), this);
        
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RTPQueuePlaceholders(this).register();
            getLogger().info("PlaceholderAPI hooked successfully!");
        }
        
        getLogger().info("RTPQueueZ v" + getDescription().getVersion() + " has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (queueManager != null) {
            queueManager.shutdown();
        }
        getLogger().info("RTPQueueZ has been disabled!");
    }
    
    public static RTPQueueZ getInstance() {
        return instance;
    }
    
    public RTPQueueManager getQueueManager() {
        return queueManager;
    }
}
