package dev.allenalt.rtpqueuez.queue;

import dev.allenalt.rtpqueuez.RTPQueueZ;
import dev.allenalt.rtpqueuez.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RTPQueueManager {
    
    private final RTPQueueZ plugin;
    private final Map<String, List<RTPQueueEntry>> queues;
    private final Map<UUID, String> playerCooldowns;
    private final Set<UUID> playersInQueue;
    
    public RTPQueueManager(RTPQueueZ plugin) {
        this.plugin = plugin;
        this.queues = new ConcurrentHashMap<>();
        this.playerCooldowns = new ConcurrentHashMap<>();
        this.playersInQueue = ConcurrentHashMap.newKeySet();
        
        queues.put("overworld", new ArrayList<>());
        queues.put("nether", new ArrayList<>());
        queues.put("end", new ArrayList<>());
    }
    
    public boolean joinQueue(Player player, String worldType) {
        if (playersInQueue.contains(player.getUniqueId())) {
            return false;
        }
        
        if (isOnCooldown(player)) {
            return false;
        }
        
        RTPQueueEntry entry = new RTPQueueEntry(player, worldType);
        queues.get(worldType).add(entry);
        playersInQueue.add(player.getUniqueId());
        
        MessageUtils.sendMessage(player, "queue-joined");
        
        if (plugin.getConfig().getBoolean("broadcast-queue-join", true)) {
            broadcastQueueJoin(player, worldType);
        }
        
        checkAndMatchPlayers(worldType);
        
        return true;
    }
    
    public boolean leaveQueue(Player player) {
        if (!playersInQueue.contains(player.getUniqueId())) {
            return false;
        }
        
        for (List<RTPQueueEntry> queue : queues.values()) {
            queue.removeIf(entry -> entry.getPlayer().getUniqueId().equals(player.getUniqueId()));
        }
        
        playersInQueue.remove(player.getUniqueId());
        MessageUtils.sendMessage(player, "queue-leaved");
        
        return true;
    }
    
    public boolean isInQueue(Player player) {
        return playersInQueue.contains(player.getUniqueId());
    }
    
    public int getQueueCount(String worldType) {
        return queues.getOrDefault(worldType, new ArrayList<>()).size();
    }
    
    private void checkAndMatchPlayers(String worldType) {
        List<RTPQueueEntry> queue = queues.get(worldType);
        int maxPlayers = plugin.getConfig().getInt("max-players", 2);
        
        if (queue.size() >= maxPlayers) {
            List<RTPQueueEntry> matched = new ArrayList<>();
            for (int i = 0; i < maxPlayers && i < queue.size(); i++) {
                matched.add(queue.get(i));
            }
            
            queue.removeAll(matched);
            
            for (RTPQueueEntry entry : matched) {
                playersInQueue.remove(entry.getPlayer().getUniqueId());
            }
            
            startTeleportCountdown(matched, worldType);
        }
    }
    
    private void startTeleportCountdown(List<RTPQueueEntry> entries, String worldType) {
        int delay = plugin.getConfig().getInt("teleport-delay", 5);
        
        for (RTPQueueEntry entry : entries) {
            Player player = entry.getPlayer();
            
            new BukkitRunnable() {
                int countdown = delay;
                
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        return;
                    }
                    
                    if (countdown > 0) {
                        MessageUtils.sendMessageWithPlaceholder(player, "teleport", "%time%", String.valueOf(countdown));
                        MessageUtils.sendTitle(player, "teleport", "%time%", String.valueOf(countdown));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        countdown--;
                    } else {
                        teleportPlayer(player, worldType);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }
    
    private void teleportPlayer(Player player, String worldType) {
        String worldName = getWorldName(worldType);
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            player.sendMessage(ChatColor.RED + "World not found!");
            return;
        }
        
        Location randomLocation = getRandomSafeLocation(world);
        player.teleport(randomLocation);
        
        setCooldown(player);
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
    
    private Location getRandomSafeLocation(World world) {
        Random random = new Random();
        int minRadius = plugin.getConfig().getInt("min-radius", 1000);
        int maxRadius = minRadius + 5000;
        
        int x = random.nextInt(maxRadius - minRadius) + minRadius;
        int z = random.nextInt(maxRadius - minRadius) + minRadius;
        
        if (random.nextBoolean()) x = -x;
        if (random.nextBoolean()) z = -z;
        
        int y = world.getHighestBlockYAt(x, z);
        
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }
    
    private String getWorldName(String worldType) {
        FileConfiguration config = plugin.getConfig();
        return switch (worldType.toLowerCase()) {
            case "nether" -> config.getString("nether-world", "world_nether");
            case "end" -> config.getString("end-world", "world_the_end");
            default -> config.getString("overworld-world", "world");
        };
    }
    
    private void broadcastQueueJoin(Player player, String worldType) {
        List<String> messages = plugin.getConfig().getStringList("messages.queue-broadcast");
        
        for (String message : messages) {
            message = message.replace("%player%", player.getName())
                           .replace("%world%", capitalizeFirst(worldType))
                           .replace("&", "§");
            Bukkit.broadcastMessage(message);
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private void setCooldown(Player player) {
        int cooldown = plugin.getConfig().getInt("cooldown", 30);
        playerCooldowns.put(player.getUniqueId(), String.valueOf(System.currentTimeMillis() + (cooldown * 1000L)));
        
        new BukkitRunnable() {
            @Override
            public void run() {
                playerCooldowns.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, cooldown * 20L);
    }
    
    private boolean isOnCooldown(Player player) {
        if (!playerCooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long cooldownEnd = Long.parseLong(playerCooldowns.get(player.getUniqueId()));
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    public void shutdown() {
        queues.clear();
        playerCooldowns.clear();
        playersInQueue.clear();
    }
}
