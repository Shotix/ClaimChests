package io.github.shotix.claimchests;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block targetBlock = player.getTargetBlockExact(6);

            if (targetBlock != null && targetBlock.getType() == Material.CHEST) {
                if (!HandleChests.isChestClaimed(targetBlock, false)) {
                    int nrOfChestsClaimed = HandleChests.writeClaimToFile(targetBlock, player);
                    if (nrOfChestsClaimed <= 4) {
                        sender.sendMessage(ChatColor.GOLD + "You have currently claimed " + ChatColor.YELLOW + nrOfChestsClaimed + ChatColor.GOLD + " Chests. You can claim " + ChatColor.YELLOW + (4 - nrOfChestsClaimed) + ChatColor.GOLD + " more Chests!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You have claimed the maximum amount of chests! Please unclaim one of your Chests first.");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "This chest is already claimed!");
                    return false;
                }
            }
        }
        return false;
    }
}
