package com.gio.heads.events;

import com.gio.heads.Heads;
import com.gio.heads.utils.Utils;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class HeadArmorClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getCurrentItem().getItemMeta() == null) {
            return;
        }


        Player player = (Player) event.getWhoClicked();
        Document document = Utils.getPlayerDoucment(player);
        List<Document> staffList = document.getList("Heads", Document.class);
        int staffListSize;
        if (staffList == null) {
            staffListSize = 0;
        } else {
            staffListSize = staffList.size();
        }
        for (int i = 0; i < staffListSize; i++) {
            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(Heads.headKey, PersistentDataType.STRING)) {
                String pdcString = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Heads.headKey, PersistentDataType.STRING);
                String staffHoofd = staffList.get(i).getString("Stafflid");
                if (pdcString.equals(staffHoofd)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
