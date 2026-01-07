package com.gio.heads.events;

import com.gio.heads.Heads;
import com.gio.heads.utils.Utils;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDate;
import java.util.List;

public class HeadPickUpListener implements Listener {
    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            ItemMeta item = event.getItem().getItemStack().getItemMeta();
            if (item.getPersistentDataContainer().has(Heads.uuidKey, PersistentDataType.STRING)) {
                String stringUUID = item.getPersistentDataContainer().get(Heads.uuidKey, PersistentDataType.STRING);

                Player player = (Player) event.getEntity();
                Document zoekDocument = new Document("UUID", player.getUniqueId().toString());
                Document doelDocument = (Document) Heads.players.find(zoekDocument).first();

                if (doelDocument != null) {
                    List<Document> staffList = doelDocument.getList("Heads", Document.class);
                    int staffListSize;
                    if (staffList == null) {
                        staffListSize = 0;
                    } else {
                        staffListSize = staffList.size();
                    }
                    for (int i = 0; i < staffListSize; i++) {
                        String staffLid = staffList.get(i).getString("UUID");
                        if (staffLid.equals(stringUUID)) {
                            System.out.println("Je probeerde iets op te pakken maar dat lukte niet");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                Document playerDocument = new Document("UUID", stringUUID);
                Document document = (Document) Heads.staffHeads.find(playerDocument).first();

                if (document != null) {
                    int uitgegevenHeads = document.getInteger("Uitgegeven heads");
                    document.put("Uitgegeven heads", uitgegevenHeads + 1);
                    Heads.staffHeads.replaceOne(playerDocument, document);
                }

                event.setCancelled(true);
                event.getItem().remove();
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                player.sendMessage(ChatColor.YELLOW + "Je hebt " + ChatColor.GOLD + document.getString("Stafflid") + "'s" + ChatColor.YELLOW + " hoofd gevonden!");

                Document headDataDocument = new Document();
                headDataDocument.put("Stafflid", document.getString("Stafflid"));
                headDataDocument.put("UUID", document.getString("UUID"));
                headDataDocument.put("Datum", LocalDate.now());

                Document spelerGevondenStaff = new Document();
                spelerGevondenStaff.put("$addToSet", new Document("Heads", headDataDocument));
                Document spelerDocument = new Document("UUID", player.getUniqueId().toString());
                Heads.players.updateOne(spelerDocument, spelerGevondenStaff);
            }
        }
    }
}
