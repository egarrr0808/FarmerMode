package com.farmermode.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FarmerMode extends JavaPlugin implements Listener {

    private boolean farmModeActive = false;
    private final Map<UUID, Boolean> voteMap = new HashMap<>();
    private BukkitTask voteTask = null;
    private FileConfiguration config;
    private SpawnRateManager spawnRateManager;
    private TabListManager tabListManager;
    private VoteManager voteManager;
    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        config = getConfig();
        
        // Initialize managers
        spawnRateManager = new SpawnRateManager(this);
        tabListManager = new TabListManager(this);
        voteManager = new VoteManager(this);
        permissionManager = new PermissionManager(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(spawnRateManager, this);
        
        // Update tab list for all players
        updateTabList();
        
        getLogger().info("FarmerMode plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Restore default spawn rates if farm mode is active
        if (farmModeActive) {
            spawnRateManager.setDefaultSpawnRates();
        }
        
        // Cancel any ongoing vote
        if (voteTask != null) {
            voteTask.cancel();
        }
        
        // Clean up permission attachments
        if (permissionManager != null) {
            permissionManager.cleanup();
        }
        
        getLogger().info("FarmerMode plugin has been disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("farmmode")) {
            if (args.length == 0) {
                sendHelpMessage(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "enable":
                    return handleEnableCommand(sender);
                case "disable":
                    return handleDisableCommand(sender);
                case "vote":
                    return handleVoteCommand(sender);
                case "status":
                    return handleStatusCommand(sender);
                default:
                    sendHelpMessage(sender);
                    return true;
            }
        }
        return false;
    }

    private boolean handleEnableCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("farmermode.admin")) {
            if (!permissionManager.isFarmer(player)) {
                player.sendMessage(Component.text("You don't have permission to use this command!").color(NamedTextColor.RED));
                return true;
            }
            
            // Start a vote if the player is a farmer but not an admin
            voteManager.startVote(player);
            return true;
        }

        // Admin direct enable
        if (farmModeActive) {
            player.sendMessage(Component.text("Farm mode is already active!").color(NamedTextColor.YELLOW));
            return true;
        }

        enableFarmMode();
        Bukkit.broadcast(
            Component.text("Farm mode has been enabled by an admin!").color(NamedTextColor.GREEN),
            "farmermode.use"
        );
        return true;
    }

    private boolean handleDisableCommand(CommandSender sender) {
        if (!sender.hasPermission("farmermode.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!").color(NamedTextColor.RED));
            return true;
        }

        if (!farmModeActive) {
            sender.sendMessage(Component.text("Farm mode is not currently active!").color(NamedTextColor.YELLOW));
            return true;
        }

        disableFarmMode();
        Bukkit.broadcast(
            Component.text("Farm mode has been disabled by an admin!").color(NamedTextColor.RED),
            "farmermode.use"
        );
        return true;
    }

    private boolean handleVoteCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("farmermode.use")) {
            player.sendMessage(Component.text("You don't have permission to use this command!").color(NamedTextColor.RED));
            return true;
        }

        if (!voteManager.isVoteActive()) {
            player.sendMessage(Component.text("There is no active vote right now!").color(NamedTextColor.YELLOW));
            return true;
        }

        // Check if player has already voted
        if (voteManager.hasPlayerVoted(player.getUniqueId())) {
            player.sendMessage(Component.text("You have already voted!").color(NamedTextColor.YELLOW));
            return true;
        }

        voteManager.castVote(player, true);
        return true;
    }

    private boolean handleStatusCommand(CommandSender sender) {
        if (!sender.hasPermission("farmermode.use")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!").color(NamedTextColor.RED));
            return true;
        }

        Component statusMessage = Component.text("Farm Mode Status: ")
            .color(NamedTextColor.GOLD)
            .append(Component.text(farmModeActive ? "ENABLED" : "DISABLED")
                .color(farmModeActive ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decorate(TextDecoration.BOLD));

        sender.sendMessage(statusMessage);

        if (voteManager.isVoteActive()) {
            int eligibleVoters = 0;
            int votesReceived = 0;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("farmermode.use")) {
                    eligibleVoters++;
                    if (voteManager.hasPlayerVoted(player.getUniqueId())) {
                        votesReceived++;
                    }
                }
            }
            
            sender.sendMessage(Component.text("A vote is currently in progress!")
                .color(NamedTextColor.YELLOW));
                
            sender.sendMessage(Component.text("Votes: ")
                .color(NamedTextColor.WHITE)
                .append(Component.text(votesReceived + "/" + eligibleVoters)
                .color(NamedTextColor.GREEN)));
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("=== FarmerMode Commands ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/farmmode enable").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Start a vote to enable farm mode (or directly enable if admin)").color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/farmmode disable").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Disable farm mode (admin only)").color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/farmmode vote").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Vote yes for farm mode during an active vote").color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/farmmode status").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Check the current status of farm mode").color(NamedTextColor.WHITE)));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update tab list for the joining player
        tabListManager.updatePlayerTabList(event.getPlayer());
    }

    public void enableFarmMode() {
        farmModeActive = true;
        spawnRateManager.setIncreasedSpawnRates();
        updateTabList();
    }

    public void disableFarmMode() {
        farmModeActive = false;
        spawnRateManager.setDefaultSpawnRates();
        updateTabList();
    }

    private void updateTabList() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            tabListManager.updatePlayerTabList(player);
        }
    }

    // Getters for other classes to access
    public boolean isFarmModeActive() {
        return farmModeActive;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }
    
    public VoteManager getVoteManager() {
        return voteManager;
    }
    
    public SpawnRateManager getSpawnRateManager() {
        return spawnRateManager;
    }
}
