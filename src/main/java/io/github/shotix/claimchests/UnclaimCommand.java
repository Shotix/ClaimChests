package io.github.shotix.claimchests;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnclaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block targetBlock = player.getTargetBlockExact(6);

            if (targetBlock != null && targetBlock.getType() == Material.CHEST) {
                if (HandleChests.isChestClaimed(targetBlock, false) && HandleChests.isChestClaimedByPlayer(player)) {
                    HandleChests.unclaimChest(targetBlock);
                    sender.sendMessage(ChatColor.GOLD + "Your Chest has been unclaimed.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "No valid target!");
        }
        return false;
    }
}
