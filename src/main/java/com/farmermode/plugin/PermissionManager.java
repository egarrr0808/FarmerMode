package com.farmermode.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager implements Listener {

    private final FarmerMode plugin;
    private final Map<UUID, PermissionAttachment> permissionAttachments = new HashMap<>();

    public PermissionManager(FarmerMode plugin) {
        this.plugin = plugin;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Check if a player has the farmer role
     */
    public boolean isFarmer(Player player) {
        return player.hasPermission("farmermode.farmer");
    }

    /**
     * Check if a player is an admin
     */
    public boolean isAdmin(Player player) {
        return player.hasPermission("farmermode.admin");
    }

    /**
     * Grant the farmer role to a player
     */
    public void grantFarmerRole(Player player) {
        PermissionAttachment attachment = getPlayerAttachment(player);
        attachment.setPermission("farmermode.farmer", true);
        attachment.setPermission("farmermode.use", true);
        
        player.recalculatePermissions();
        
        plugin.getLogger().info("Granted farmer role to " + player.getName());
    }

    /**
     * Revoke the farmer role from a player
     */
    public void revokeFarmerRole(Player player) {
        PermissionAttachment attachment = getPlayerAttachment(player);
        attachment.setPermission("farmermode.farmer", false);
        
        // Keep the basic use permission if they're an admin
        if (!player.hasPermission("farmermode.admin")) {
            attachment.setPermission("farmermode.use", false);
        }
        
        player.recalculatePermissions();
        
        plugin.getLogger().info("Revoked farmer role from " + player.getName());
    }

    /**
     * Get or create a permission attachment for a player
     */
    private PermissionAttachment getPlayerAttachment(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (permissionAttachments.containsKey(playerId)) {
            return permissionAttachments.get(playerId);
        }
        
        PermissionAttachment attachment = player.addAttachment(plugin);
        permissionAttachments.put(playerId, attachment);
        return attachment;
    }

    /**
     * Handle player join events to set up permissions
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Create permission attachment for the player
        if (!permissionAttachments.containsKey(player.getUniqueId())) {
            permissionAttachments.put(player.getUniqueId(), player.addAttachment(plugin));
        }
        
        // Check if player has op status and grant admin permission if needed
        if (player.isOp() && !player.hasPermission("farmermode.admin")) {
            PermissionAttachment attachment = permissionAttachments.get(player.getUniqueId());
            attachment.setPermission("farmermode.admin", true);
            attachment.setPermission("farmermode.use", true);
            player.recalculatePermissions();
        }
    }

    /**
     * Clean up permission attachments when the plugin is disabled
     */
    public void cleanup() {
        permissionAttachments.clear();
    }
}
