package io.github.shotix.claimchests;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ClaimChests extends JavaPlugin {

    private static ClaimChests instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        getServer().getPluginManager().registerEvents(new Listeners(), this);

        Objects.requireNonNull(getCommand("claim")).setExecutor(new ClaimCommand());
        Objects.requireNonNull(getCommand("unclaim")).setExecutor(new UnclaimCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        instance = null;
    }
}
