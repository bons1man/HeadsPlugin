package com.gio.heads;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;

public final class Heads extends JavaPlugin {

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection staffHeads;
    private MongoCollection players;

    public NamespacedKey uuidKey;
    public NamespacedKey headKey;

    @Override
    public void onEnable() {
        uuidKey = new NamespacedKey(this, "UUID");
        headKey = new NamespacedKey(this, "head");

        client = new MongoClient();
        database =  client.getDatabase("heads");
        players = database.getCollection("players");
        staffHeads = database.getCollection("staffheads");

        getServer().getPluginManager().registerEvents(new HeadsListener(this), this);

        var lamp = BukkitLamp.builder(this).build();
        lamp.register(new HeadsCommands(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection getStaffHeads() {
        return staffHeads;
    }

    public MongoCollection getPlayers() {
        return players;
    }
}
