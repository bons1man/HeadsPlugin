package com.gio.heads.events;

import com.gio.heads.Heads;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DropStaffdexListener implements Listener {
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item =  event.getItemDrop().getItemStack();
        if (item == null) {
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        if (itemMeta.getPersistentDataContainer().has(Heads.headKey)) {
            String s = itemMeta.getPersistentDataContainer().get(Heads.headKey, PersistentDataType.STRING);
            if (s.equals("Staffdex")) {
                event.setCancelled(true);
            }
        }
    }
}
