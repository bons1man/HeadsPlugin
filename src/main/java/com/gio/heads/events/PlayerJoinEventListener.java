package com.gio.heads.events;

import com.gio.heads.Heads;
import com.gio.heads.utils.Utils;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerJoinEventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ==";
        ItemStack head = Utils.createSkull(texture);

        SkullMeta headItemMeta = (SkullMeta) head.getItemMeta();
        headItemMeta.getPersistentDataContainer().set(Heads.headKey, PersistentDataType.STRING, "Staffdex");
        head.setItemMeta(headItemMeta);

        PlayerInventory inventory = player.getInventory();
        boolean found = false;
        for (int i = 0; i < 41; i ++) {
            ItemStack is = inventory.getItem(i);
            if (is == null) {
                continue;
            }
            ItemMeta im = is.getItemMeta();
            if (im.getPersistentDataContainer().has(Heads.headKey, PersistentDataType.STRING)) {
                String s = im.getPersistentDataContainer().get(Heads.headKey, PersistentDataType.STRING);
                if (s.equals("Staffdex")) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            inventory.addItem(head);
        }

        Document document = Utils.getPlayerDoucment(player);

        if (document == null) {
            Document newDocument = new Document();
            newDocument.put("Username", player.getName());
            newDocument.put("UUID", player.getUniqueId().toString());
            Heads.players.insertOne(newDocument);
        }
    }
}
