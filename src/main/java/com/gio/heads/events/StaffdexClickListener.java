package com.gio.heads.events;

import com.gio.heads.Heads;
import com.gio.heads.utils.Utils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class StaffdexClickListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand() == null) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType().equals(Material.PLAYER_HEAD)) {
            if (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Staff" + ChatColor.GRAY + "dex")) {
                event.setCancelled(true);
            }
        }
    }

    //Toetevoegen:
    //Als de speler klikt op een hoofd die hij al draagt een message naar de speler sturen en hem niet weer opdoen
    //Niet checken op Material Type en Naam maar op PDC

    //Bug:
    //Als een speler in creative zit kan hij soms ze hoofd clonen?? :crying_face:
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.getInventory().getItemInMainHand() == null) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getItemMeta() == null) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(Heads.headKey)) {
            String s = player.getInventory().getItemInMainHand().getPersistentDataContainer().get(Heads.headKey, PersistentDataType.STRING);
            if (s.equals("Staffdex")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    PaginatedGui headCollection = Gui.paginated().title(Component.text(ChatColor.LIGHT_PURPLE + "Staff" + ChatColor.GRAY + "dex")).rows(6).pageSize(45).create();

                    headCollection.setDefaultClickAction(event -> {
                        event.setCancelled(true);
                    });

                    Document document = Utils.getPlayerDoucment(player);
                    List<Document> staffList = document.getList("Heads", Document.class);
                    int staffListSize;
                    if (staffList == null) {
                        staffListSize = 0;
                    } else {
                        staffListSize = staffList.size();
                    }

                    Utils.addGuiItems(staffListSize, staffList, headCollection);

                    GuiItem removeHead = ItemBuilder
                            .from(Material.BARRIER)
                            .setName(ChatColor.RED + "Haal je staffhoofd weg")
                            .asGuiItem(event -> {
                                PlayerInventory inv = player.getInventory();
                                if (inv.getHelmet() != null) {
                                    ItemStack helmet = inv.getHelmet();
                                    if (helmet.getType() == Material.PLAYER_HEAD) {
                                        SkullMeta headMeta = (SkullMeta) helmet.getItemMeta();
                                        if (headMeta.getPersistentDataContainer().has(Heads.headKey, PersistentDataType.STRING)) {
                                            player.sendMessage(ChatColor.RED + "Je hebt je staff hoofd afgedaan");
                                            inv.setHelmet(null);
                                            headCollection.close(player);
                                        } else {
                                            player.sendMessage(ChatColor.RED + "Je hebt geen staff hoofd aan!");
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Je hebt geen staff hoofd aan!");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Je hebt geen staff hoofd aan!");
                                }
                            });
                    headCollection.setItem(49, removeHead);
                    headCollection.setItem(48, ItemBuilder.from(Material.PAPER).setName(ChatColor.RED + "Vorige").asGuiItem(event -> headCollection.previous()));
                    headCollection.setItem(50, ItemBuilder.from(Material.PAPER).setName(ChatColor.RED + "Volgende").asGuiItem(event -> headCollection.next()));

                    for (int i = 46; i < 53; i++) {
                        switch(i) {
                            case 48:
                                continue;
                            case 49:
                                continue;
                            case 50:
                                continue;
                        }
                        headCollection.setItem(i, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).asGuiItem());
                    }
                    String clockTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTczMWE2ZmU1MDNlODY1MjI5OTZhMDg3YjNiMjRmZDM2NTk2OTgzZGIwMzg0NjY3N2MyYWJmMzIzYWE4NTc2In19fQ==";
                    headCollection.setItem(45, ItemBuilder.from(Utils.createSkull(clockTexture)).setName(ChatColor.RED + "Sorteer op datum").asGuiItem(clickEvent -> {
                        List<Document> gesorteerdeList = staffList;
                        headCollection.clearPageItems();
                        gesorteerdeList.sort(new Comparator<Document>() {
                            @Override
                            public int compare(Document o1, Document o2) {
                                Date d1 = o1.getDate("Datum");
                                Date d2 = o2.getDate("Datum");
                                return d1.compareTo(d2);
                            }
                        });

                        Utils.addGuiItems(staffListSize, gesorteerdeList, headCollection);

                        headCollection.update();
                    }));
                    String bookTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjhmYmVlMmNkZjQ0MDYxNzZlZGE5Y2Y5MjBiOWNlNjk1OTBhMDQ5MGM3MjRhZTZjYzJlMmYzMTZlNWU3ZGQxZCJ9fX0=";
                    headCollection.setItem(53, ItemBuilder.from(Utils.createSkull(bookTexture)).setName(ChatColor.RED + "Sorteer van A-Z").asGuiItem(clickEvent -> {
                        List<Document> gesorteerdeList = staffList;
                        headCollection.clearPageItems();
                        gesorteerdeList.sort(new Comparator<Document>() {
                            @Override
                            public int compare(Document o1, Document o2) {
                                String s1 = o1.getString("Stafflid");
                                String s2 = o2.getString("Stafflid");
                                return s1.compareToIgnoreCase(s2);
                            }
                        });

                        Utils.addGuiItems(staffListSize, gesorteerdeList, headCollection);

                        headCollection.update();
                    }));

                    headCollection.open(player);
                }
            }
        }
    }
}
