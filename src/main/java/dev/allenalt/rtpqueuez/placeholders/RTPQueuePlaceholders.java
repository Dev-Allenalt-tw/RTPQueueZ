package dev.allenalt.rtpqueuez.placeholders;

import dev.allenalt.rtpqueuez.RTPQueueZ;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RTPQueuePlaceholders extends PlaceholderExpansion {
    
    private final RTPQueueZ plugin;
    
    public RTPQueuePlaceholders(RTPQueueZ plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "Dev_Allenalt_tw";
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "rtpqueue";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("count_overworld")) {
            return String.valueOf(plugin.getQueueManager().getQueueCount("overworld"));
        }
        
        if (params.equalsIgnoreCase("count_nether")) {
            return String.valueOf(plugin.getQueueManager().getQueueCount("nether"));
        }
        
        if (params.equalsIgnoreCase("count_end")) {
            return String.valueOf(plugin.getQueueManager().getQueueCount("end"));
        }
        
        if (params.equalsIgnoreCase("status")) {
            if (player != null) {
                return plugin.getQueueManager().isInQueue(player) ? "In Queue" : "Not in Queue";
            }
        }
        
        return null;
    }
}
