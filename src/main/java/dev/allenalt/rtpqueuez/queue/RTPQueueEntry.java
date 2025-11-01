package dev.allenalt.rtpqueuez.queue;

import org.bukkit.entity.Player;

public class RTPQueueEntry {
    
    private final Player player;
    private final String worldType;
    private final long joinTime;
    
    public RTPQueueEntry(Player player, String worldType) {
        this.player = player;
        this.worldType = worldType;
        this.joinTime = System.currentTimeMillis();
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getWorldType() {
        return worldType;
    }
    
    public long getJoinTime() {
        return joinTime;
    }
}
