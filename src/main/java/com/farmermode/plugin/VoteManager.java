package com.farmermode.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteManager {

    private final FarmerMode plugin;
    private boolean voteActive = false;
    private final Map<UUID, Boolean> voteMap = new HashMap<>();
    private BukkitTask voteTask = null;
    private final int voteTimeSeconds;
    private final double requiredApprovalPercentage;

    public VoteManager(FarmerMode plugin) {
        this.plugin = plugin;
        this.voteTimeSeconds = plugin.getPluginConfig().getInt("vote-time-seconds", 60);
        this.requiredApprovalPercentage = plugin.getPluginConfig().getDouble("required-approval-percentage", 75.0);
    }

    /**
     * Start a vote to enable farm mode
     */
    public void startVote(Player initiator) {
        if (voteActive) {
            initiator.sendMessage(Component.text("A vote is already in progress!").color(NamedTextColor.RED));
            return;
        }

        if (plugin.isFarmModeActive()) {
            initiator.sendMessage(Component.text("Farm mode is already active!").color(NamedTextColor.YELLOW));
            return;
        }

        // Clear previous votes
        voteMap.clear();
        voteActive = true;

        // Automatically count the initiator's vote as yes
        voteMap.put(initiator.getUniqueId(), true);

        // Broadcast vote start message
        Bukkit.broadcast(
            Component.text("=== FARM MODE VOTE ===").color(NamedTextColor.GOLD),
            "farmermode.use"
        );
        
        Bukkit.broadcast(
            Component.text(initiator.getName())
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" has started a vote to enable farm mode!").color(NamedTextColor.WHITE)),
            "farmermode.use"
        );
        
        Bukkit.broadcast(
            Component.text("Type ")
                .color(NamedTextColor.WHITE)
                .append(Component.text("/farmmode vote").color(NamedTextColor.GREEN))
                .append(Component.text(" to vote YES. The vote will end in " + voteTimeSeconds + " seconds.").color(NamedTextColor.WHITE)),
            "farmermode.use"
        );

        // Schedule vote end
        voteTask = Bukkit.getScheduler().runTaskLater(plugin, this::endVote, voteTimeSeconds * 20L);
    }

    /**
     * Cast a vote for farm mode
     */
    public void castVote(Player player, boolean vote) {
        if (!voteActive) {
            player.sendMessage(Component.text("There is no active vote right now!").color(NamedTextColor.RED));
            return;
        }

        // Record the player's vote
        voteMap.put(player.getUniqueId(), vote);
        
        player.sendMessage(Component.text("Your vote has been recorded!").color(NamedTextColor.GREEN));
        
        // Broadcast the vote
        Bukkit.broadcast(
            Component.text(player.getName())
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" has voted ").color(NamedTextColor.WHITE))
                .append(Component.text("YES").color(NamedTextColor.GREEN))
                .append(Component.text(" for farm mode!").color(NamedTextColor.WHITE)),
            "farmermode.use"
        );
        
        // Check if we should end the vote early (if everyone has voted)
        checkEarlyVoteEnd();
    }

    /**
     * End the current vote and tally results
     */
    private void endVote() {
        if (!voteActive) {
            return;
        }

        voteActive = false;
        
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }

        // Count eligible voters (players with farmermode.use permission)
        int eligibleVoters = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("farmermode.use")) {
                eligibleVoters++;
            }
        }

        // Count yes votes
        int yesVotes = 0;
        for (Boolean vote : voteMap.values()) {
            if (vote) {
                yesVotes++;
            }
        }

        // Calculate approval percentage
        double approvalPercentage = (eligibleVoters > 0) 
            ? ((double) yesVotes / eligibleVoters) * 100.0 
            : 0.0;

        // Broadcast results
        Bukkit.broadcast(
            Component.text("=== VOTE RESULTS ===").color(NamedTextColor.GOLD),
            "farmermode.use"
        );
        
        Bukkit.broadcast(
            Component.text("Yes votes: ")
                .color(NamedTextColor.WHITE)
                .append(Component.text(yesVotes + "/" + eligibleVoters).color(NamedTextColor.GREEN))
                .append(Component.text(" (" + String.format("%.1f", approvalPercentage) + "%)").color(NamedTextColor.YELLOW)),
            "farmermode.use"
        );

        // Check if vote passed
        if (approvalPercentage >= requiredApprovalPercentage) {
            Bukkit.broadcast(
                Component.text("The vote has passed! Enabling farm mode...").color(NamedTextColor.GREEN),
                "farmermode.use"
            );
            
            plugin.enableFarmMode();
        } else {
            Bukkit.broadcast(
                Component.text("The vote has failed! Farm mode will not be enabled.").color(NamedTextColor.RED),
                "farmermode.use"
            );
        }
    }

    /**
     * Check if we can end the vote early (if everyone has voted)
     */
    private void checkEarlyVoteEnd() {
        int eligibleVoters = 0;
        int totalVotes = voteMap.size();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("farmermode.use")) {
                eligibleVoters++;
            }
        }

        // If everyone has voted, end the vote
        if (totalVotes >= eligibleVoters) {
            Bukkit.getScheduler().runTask(plugin, this::endVote);
        }
    }

    /**
     * Check if a vote is currently active
     */
    public boolean isVoteActive() {
        return voteActive;
    }
    
    /**
     * Check if a player has already voted
     */
    public boolean hasPlayerVoted(UUID playerId) {
        return voteMap.containsKey(playerId);
    }
}
