package com.emerald.thunder;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.world.LootGenerateEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.loot.LootTables;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class LifePlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        getServer().getScheduler().scheduleSyncRepeatingTask(
                this,
                new Runnable() {
                    public void run() {
                        updatePlayers();
                    }
                },
                0L,
                1L
        );
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String name = event.getEntity().getName();

        String path = "data.player." + name + ".lives";
        getConfig().set(path, getConfig().getInt(path) - 1);
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();

        getLogger().info(String.valueOf(getConfig().getBoolean("data.player." + name + ".exists")));

        if (!(getConfig().getBoolean("data.player." + name + ".exists"))) {
            getLogger().info("true");
            getConfig().set("data.player." + name + ".lives", getConfig().getInt("config.start-lives"));
            getConfig().set("data.player." + name + ".exists", true);
            getConfig().set("data.player." + name + ".dead", false);
            saveConfig();
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        Random random = new Random();
        int min = 0;
        int max = 0;

        if (event.getLootTable().getKey().equals(LootTables.BASTION_TREASURE.getKey())) {
            min = 1;
            max = 2;
        } else if (event.getLootTable().getKey().equals(LootTables.SHIPWRECK_TREASURE.getKey())) {
            min = 1;
            max = 30;
        } else if (event.getLootTable().getKey().equals(LootTables.DESERT_PYRAMID.getKey())) {
            min = 1;
            max = 25;
        } else if (event.getLootTable().getKey().equals(LootTables.SIMPLE_DUNGEON.getKey())) {
            min = 1;
            max = 50;
        } else if (event.getLootTable().getKey().equals(LootTables.RUINED_PORTAL.getKey())) {
            min = 1;
            max = 100;
        } else if (event.getLootTable().getKey().equals(LootTables.ANCIENT_CITY_ICE_BOX.getKey())) {
            min = 1;
            max = 1;
        } else if (event.getLootTable().getKey().equals(LootTables.END_CITY_TREASURE.getKey())) {
            min = 1;
            max = 10;
        }

        int result = random.nextInt(min, max + 1);

        if (result == 1) {
            List<ItemStack> items = new ArrayList<>(event.getLoot());
            ItemStack stack = new ItemStack(Material.TOTEM_OF_UNDYING);
            ItemMeta meta = stack.getItemMeta();

            List<String> lore = new ArrayList<String>();
            lore.add(ChatColor.RED + "Take this item to get an extra life");

            meta.setDisplayName(ChatColor.GOLD + "Life container");
            meta.setLore(lore);

            items.add(stack);

            stack.setItemMeta(meta);

            if (items != null) {
                getLogger().info("True");
            }

            event.setLoot(items);
        }
    }

    public void updatePlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            String name = player.getName();
            String path = "data.player." + name + ".lives";

            if (player.getHealth() > 0) {
                if (getConfig().getInt(path) < 1) {
                    player.setGameMode(GameMode.SPECTATOR);
                    if (!(getConfig().getBoolean("data.player." + name + ".dead"))) {
                        player.sendTitle("GAME OVER!", "You have lost all your lives!", 4, 30, 4);
                        getConfig().set("data.player." + name + ".dead", true);
                        saveConfig();
                    }
                }
            }

            if (inInventory(player.getName(), Material.TOTEM_OF_UNDYING)) {
                player.playEffect(EntityEffect.TOTEM_RESURRECT);
                player.sendTitle("You have earned another life!", "Totem activated!", 4, 30, 4);
                getServer().broadcastMessage(name + " has gained a life!");
                getConfig().set(path, getConfig().getInt(path) + 1);
                saveConfig();

                player.getInventory().remove(Material.TOTEM_OF_UNDYING);
            }
        }
    }

    public boolean inInventory(String name, Material item) {
        Player player = getServer().getPlayer(name);

        if (player.getInventory().contains(item)) {
            return true;
        }

        return false;
    }
}
