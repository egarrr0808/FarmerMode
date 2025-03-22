package com.farmermode.plugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SpawnRateManager implements Listener {

    private final FarmerMode plugin;
    private final FileConfiguration config;
    private final double spawnRateMultiplier;
    
    // Default spawn rate values to restore when farm mode is disabled
    private double defaultMonsterSpawnLimit;
    private long defaultTicksPerMonsterSpawns;
    
    public SpawnRateManager(FarmerMode plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        
        // Load configuration or set defaults
        this.spawnRateMultiplier = config.getDouble("spawn-rate-multiplier", 2.0);
        
        // Store current spawn rate settings
        saveDefaultSpawnRates();
    }
    
    /**
     * Save the current spawn rate settings to restore later
     */
    private void saveDefaultSpawnRates() {
        World mainWorld = Bukkit.getWorlds().get(0);
        defaultMonsterSpawnLimit = mainWorld.getMonsterSpawnLimit();
        defaultTicksPerMonsterSpawns = mainWorld.getTicksPerMonsterSpawns();
        
        // Log the default values
        plugin.getLogger().info("Default spawn settings saved:");
        plugin.getLogger().info("Monster Spawn Limit: " + defaultMonsterSpawnLimit);
        plugin.getLogger().info("Ticks Per Monster Spawns: " + defaultTicksPerMonsterSpawns);
    }
    
    /**
     * Set increased spawn rates for farm mode
     */
    public void setIncreasedSpawnRates() {
        for (World world : Bukkit.getWorlds()) {
            // Increase monster spawn limit
            world.setMonsterSpawnLimit((int) (defaultMonsterSpawnLimit * spawnRateMultiplier));
            
            // Decrease ticks between monster spawns (faster spawning)
            world.setTicksPerMonsterSpawns((int) (defaultTicksPerMonsterSpawns / spawnRateMultiplier));
        }
        
        plugin.getLogger().info("Increased spawn rates applied for farm mode");
    }
    
    /**
     * Restore default spawn rates
     */
    public void setDefaultSpawnRates() {
        for (World world : Bukkit.getWorlds()) {
            world.setMonsterSpawnLimit((int) defaultMonsterSpawnLimit);
            world.setTicksPerMonsterSpawns((int) defaultTicksPerMonsterSpawns);
        }
        
        plugin.getLogger().info("Default spawn rates restored");
    }
    
    /**
     * Monitor creature spawn events for debugging
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.isFarmModeActive() && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            // For debugging purposes, we could log spawn events or modify them further
            // This is a hook point for additional spawn customization if needed
        }
    }
}
