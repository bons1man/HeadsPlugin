package com.gio.heads;

import com.gio.heads.commands.SpawnHeadCommand;
import com.gio.heads.events.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;

public final class Heads extends JavaPlugin {

    public static MongoClient client;
    public static MongoDatabase database;
    public static MongoCollection staffHeads;
    public static MongoCollection players;

    public static NamespacedKey uuidKey;
    public static NamespacedKey headKey;

    @Override
    public void onEnable() {
        uuidKey = new NamespacedKey(this, "UUID");
        headKey = new NamespacedKey(this, "head");

        client = new MongoClient();
        database =  client.getDatabase("heads");
        players = database.getCollection("players");
        staffHeads = database.getCollection("staffheads");

        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), this);
        getServer().getPluginManager().registerEvents(new StaffdexClickListener(), this);
        getServer().getPluginManager().registerEvents(new DropStaffdexListener(), this);
        getServer().getPluginManager().registerEvents(new HeadArmorClickListener(), this);
        getServer().getPluginManager().registerEvents(new HeadPickUpListener(), this);

        var lamp = BukkitLamp.builder(this).build();
        lamp.register(new SpawnHeadCommand(this));
    }

    @Override
    public void onDisable() {
    }
}
