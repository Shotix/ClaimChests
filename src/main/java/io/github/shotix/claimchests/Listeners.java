package io.github.shotix.claimchests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.regex.MatchResult;

public class Listeners implements Listener {

    @EventHandler
    public static void event(PlayerInteractEvent playerInteractEvent) {
        if (Objects.requireNonNull(playerInteractEvent.getClickedBlock()).getType() == Material.CHEST) {
            if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (HandleChests.isChestClaimed(playerInteractEvent.getClickedBlock(), false)) {
                    if (HandleChests.isChestClaimedByPlayer(playerInteractEvent.getPlayer())) {
                    } else {
                        playerInteractEvent.getPlayer().sendMessage(ChatColor.RED + "This Chest is claimed by another user.");
                        playerInteractEvent.setCancelled(true);
                    }

                }
            }
        }
    }

    @EventHandler
    public static void event(BlockPlaceEvent blockPlaceEvent) {
        if (blockPlaceEvent.getBlock().getType() == Material.CHEST) {
            if (!HandleChests.isChestPlacementValid(blockPlaceEvent)) {
                blockPlaceEvent.getPlayer().sendMessage(ChatColor.RED + "You can't place a Chest at the side of an already claimed Chest. Please unclaim the Chest first.");
                blockPlaceEvent.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void event(BlockBreakEvent blockBreakEvent) {
        if (blockBreakEvent.getBlock().getType() == Material.CHEST) {
            if (HandleChests.isChestClaimed(blockBreakEvent.getBlock(), false)) {
                blockBreakEvent.setCancelled(true);
                blockBreakEvent.getPlayer().sendMessage(ChatColor.RED + "This Chest is claimed. Please unclaim it first and destroy the Chest afterwords.");
            }
        }
    }
}
