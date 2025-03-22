package com.farmermode.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class TabListManager {

    private final FarmerMode plugin;

    public TabListManager(FarmerMode plugin) {
        this.plugin = plugin;
    }

    /**
     * Update the tab list for a specific player
     */
    public void updatePlayerTabList(Player player) {
        Component header = createHeader();
        Component footer = createFooter();
        
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    /**
     * Create the tab list header with server status
     */
    private Component createHeader() {
        // Get server name from config
        String configServerName = plugin.getPluginConfig().getString("server-name", "Kesarikov.online");
        
        Component serverName = Component.text(configServerName)
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD);
                
        Component statusLabel = Component.text("Server Mode: ")
                .color(NamedTextColor.YELLOW);
                
        Component statusValue = Component.text(plugin.isFarmModeActive() ? "FARM MODE" : "NORMAL MODE")
                .color(plugin.isFarmModeActive() ? NamedTextColor.GREEN : NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD);
        
        Component voteStatus = Component.empty();
        if (plugin.getVoteManager().isVoteActive()) {
            voteStatus = Component.newline()
                .append(Component.text("Vote in progress! ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text("Type /farmmode vote")
                        .color(NamedTextColor.GREEN)));
        }
                
        return Component.empty()
                .append(serverName)
                .append(Component.newline())
                .append(statusLabel)
                .append(statusValue)
                .append(voteStatus);
    }

    /**
     * Create the tab list footer with additional information
     */
    private Component createFooter() {
        if (!plugin.isFarmModeActive()) {
            return Component.empty();
        }
        
        // Get spawn rate multiplier from config
        double multiplier = plugin.getPluginConfig().getDouble("spawn-rate-multiplier", 2.0);
        
        Component farmModeInfo = Component.text("Farm Mode Active!")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD);
                
        Component spawnRateInfo = Component.text("Monster spawn rates increased by ")
                .color(NamedTextColor.WHITE)
                .append(Component.text(String.format("%.1fx", multiplier))
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD));
        
        return Component.empty()
                .append(farmModeInfo)
                .append(Component.newline())
                .append(spawnRateInfo);
    }
}
