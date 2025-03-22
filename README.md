# FarmerMode Plugin Documentation

## Overview
FarmerMode is a Minecraft Paper plugin for version 1.21.4 that allows players with the "farmer" role to enable a special mode with increased monster spawn rates. This plugin includes a voting system that requires 75% approval from players to activate farm mode, and displays the current server mode in the tab list.

## Features
- **Farmer Role System**: Players with the "farmer" role can initiate votes to enable farm mode
- **Farm Mode**: Increases monster spawn rates to help with mob farming
- **Voting System**: Requires 75% approval from eligible players to enable farm mode
- **Tab List Display**: Shows the current server mode (normal or farm mode) in the tab list
- **Admin Controls**: Server administrators can directly enable or disable farm mode

## Commands
- `/farmmode enable` - Start a vote to enable farm mode (or directly enable if admin)
- `/farmmode disable` - Disable farm mode (admin only)
- `/farmmode vote` - Vote yes for farm mode during an active vote
- `/farmmode status` - Check the current status of farm mode

## Permissions
- `farmermode.use` - Allows players to use the basic farmmode commands and vote
- `farmermode.farmer` - Identifies a player as a farmer who can activate farm mode
- `farmermode.admin` - Allows administrative control over the plugin

## Configuration
The plugin's configuration file (`config.yml`) contains the following options:

```yaml
# Server display settings
server-name: "Kesarikov.online"  # Server name to display in tab list

# Spawn rate settings
spawn-rate-multiplier: 2.0  # Multiplier for monster spawn rates in farm mode

# Voting system settings
vote-time-seconds: 60       # Duration of voting period in seconds
required-approval-percentage: 75.0  # Percentage of players required to approve farm mode (0-100)

# Permission settings
auto-grant-farmer-role-to-ops: true  # Automatically grant farmer role to server operators
```

## Installation
1. Download the `FarmerMode-1.0-SNAPSHOT.jar` file
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or use a plugin manager to load the plugin
4. Configure permissions for your players using a permissions plugin

## Usage Examples

### For Farmers
1. A player with the `farmermode.farmer` permission can type `/farmmode enable` to start a vote
2. Other players will see a notification in chat and in the tab list
3. Players can vote by typing `/farmmode vote`
4. If 75% or more of eligible players vote yes, farm mode will be enabled
5. The tab list will update to show that farm mode is active

### For Admins
1. An admin with the `farmermode.admin` permission can type `/farmmode enable` to directly enable farm mode without a vote
2. Admins can disable farm mode at any time with `/farmmode disable`
3. Admins can check the current status with `/farmmode status`

## Troubleshooting
- If players cannot start votes, check that they have the `farmermode.farmer` permission
- If the tab list is not updating, ensure that the server is running Paper 1.21.4 or newer
- If monster spawn rates don't seem to increase, check that the `spawn-rate-multiplier` in the config is set to a value greater than 1.0

## Support
For support or to report issues you can contact me through Discord : _egarrr.