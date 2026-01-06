package com.gio.heads;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class HeadsListener implements Listener {

    private Heads plugin;

    public HeadsListener(Heads plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ==";
        ItemStack head = createSkull(texture);
        PlayerInventory inventory = player.getInventory();

        //Dit werkt niet omdat de head niet gelijk is aan head in inv omdat er een hele nieuwe met rndm UUID wordt gemaakt
        //Nog gefixed worden dat als een speler joined dat hij niet nog een mooie staffdex krijgt
        if (inventory.contains(head)) {
            return;
        } else {
            inventory.addItem(head);
        }

        Document document = getPlayerDoucment(player);

        if (document == null) {
            Document newDocument = new Document();
            newDocument.put("Username", player.getName());
            newDocument.put("UUID", player.getUniqueId().toString());
            plugin.getPlayers().insertOne(newDocument);
        }
    }

    public ItemStack createPlayerSkull(String name) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        OfflinePlayer speler = Bukkit.getOfflinePlayer(name);
        PlayerProfile spelerProfile = speler.getPlayerProfile();

        try {
            spelerProfile.complete(true);
        } catch (Exception exc) {
            System.out.println("Kon speler niet laden");
        }

        skullMeta.setPlayerProfile(spelerProfile);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    public ItemStack createSkull(String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
        playerProfile.setProperty(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(playerProfile);
        skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Staff" + ChatColor.GRAY + "dex");
        skull.setItemMeta(skullMeta);

        return skull;
    }

    public Document getPlayerDoucment(Player player) {
        Document spelerDocument = new Document("UUID", player.getUniqueId().toString());
        Document document = (Document) plugin.getPlayers().find(spelerDocument).first();

        if (document == null) {
            return null;
        }

        return document;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getCurrentItem().getItemMeta() == null) {
            return;
        }


        Player player = (Player) event.getWhoClicked();
        Document document = getPlayerDoucment(player);
        List<Document> staffList = document.getList("Heads", Document.class);
        int staffListSize;
        if (staffList == null) {
            staffListSize = 0;
        } else {
            staffListSize = staffList.size();
        }
        for (int i = 0; i < staffListSize; i++) {
            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.headKey, PersistentDataType.STRING)) {
                String pdcString = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.headKey, PersistentDataType.STRING);
                String staffHoofd = staffList.get(i).getString("Stafflid");
                if (pdcString.equals(staffHoofd)) {
                    event.setCancelled(true);
                    return;
                }
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

        if (player.getInventory().getItemInMainHand().getType() == Material.PLAYER_HEAD) {
            if (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Staff" + ChatColor.GRAY + "dex")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    PaginatedGui headCollection = Gui.paginated().title(Component.text(ChatColor.LIGHT_PURPLE + "Staff" + ChatColor.GRAY + "dex")).rows(6).pageSize(45).create();

                    headCollection.setDefaultClickAction(event -> {
                        event.setCancelled(true);
                    });

                    Document document = getPlayerDoucment(player);
                    List<Document> staffList = document.getList("Heads", Document.class);
                    int staffListSize;
                    if (staffList == null) {
                        staffListSize = 0;
                    } else {
                        staffListSize = staffList.size();
                    }

                    addGuiItems(staffListSize, staffList, headCollection);

                    GuiItem removeHead = ItemBuilder
                            .from(Material.BARRIER)
                            .setName(ChatColor.RED + "Haal je staffhoofd weg")
                            .asGuiItem(event -> {
                        PlayerInventory inv = player.getInventory();
                        if (inv.getHelmet() != null) {
                            ItemStack helmet = inv.getHelmet();
                            if (helmet.getType() == Material.PLAYER_HEAD) {
                                SkullMeta headMeta = (SkullMeta) helmet.getItemMeta();
                                if (headMeta.getPersistentDataContainer().has(plugin.headKey, PersistentDataType.STRING)) {
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
                    headCollection.setItem(45, removeHead);
                    headCollection.setItem(48, ItemBuilder.from(Material.PAPER).setName(ChatColor.RED + "Vorige").asGuiItem(event -> headCollection.previous()));
                    headCollection.setItem(50, ItemBuilder.from(Material.PAPER).setName(ChatColor.RED + "Volgende").asGuiItem(event -> headCollection.next()));

                    headCollection.setItem(52, ItemBuilder.from(Material.REDSTONE).setName(ChatColor.RED + "Sorteer op datum").asGuiItem(clickEvent -> {
                        List<Document> gesorteerdeList = staffList;
                        headCollection.clearPageItems();
                        gesorteerdeList.sort(new Comparator<Document>() {
                            @Override
                            public int compare(Document o1, Document o2) {
                                Date s1 = o1.getDate("Datum");
                                Date s2 = o2.getDate("Datum");
                                return s1.compareTo(s2);
                            }
                        });

                        addGuiItems(staffListSize, gesorteerdeList, headCollection);

                        headCollection.update();
                    }));
                    headCollection.setItem(53, ItemBuilder.from(Material.DIAMOND).setName(ChatColor.RED + "Sorteer van A-Z").asGuiItem(clickEvent -> {
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

                        addGuiItems(staffListSize, gesorteerdeList, headCollection);

                        headCollection.update();
                    }));

                    headCollection.open(player);
                }
            }
        }
    }

    public void addGuiItems(int staffListSize, List<Document> list, PaginatedGui gui) {
        for (int i = 0; i < staffListSize; i++) {
            String staffLid = list.get(i).getString("Stafflid");
            Date oudeDatum = list.get(i).getDate("Datum");
            LocalDate datumGevonden = oudeDatum.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatDatum = DateTimeFormatter.ofPattern("dd-MM-yyy");
            String datumString = datumGevonden.format(formatDatum);

            ItemStack staffHead = createPlayerSkull(staffLid);

            GuiItem head = ItemBuilder.from(staffHead)
                    .setName(ChatColor.RED + staffLid + "'s hoofd")
                    .setLore(ChatColor.GRAY + "Click hier om " + ChatColor.GREEN + staffLid + "'s" + ChatColor.GRAY + " hoofd op te doen!")
                    .addLore(ChatColor.GRAY + "Gevonden op: " + ChatColor.DARK_GRAY + datumString)
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        PlayerInventory inv = p.getInventory();

                        ItemStack newSkull = createPlayerSkull(staffLid);
                        SkullMeta newSkullMeta = (SkullMeta) newSkull.getItemMeta();
                        PersistentDataContainer persistentDataContainer = newSkullMeta.getPersistentDataContainer();
                        persistentDataContainer.set(plugin.headKey, PersistentDataType.STRING, staffLid);
                        newSkull.setItemMeta(newSkullMeta);

                        if (inv.getHelmet() != null) {
                            if (inv.getHelmet().getType() != Material.PLAYER_HEAD) {
                                ItemStack helmet = inv.getHelmet();
                                inv.addItem(helmet);
                            }
                        }

                        inv.setHelmet(newSkull);
                        p.sendMessage(ChatColor.GRAY + "Je hebt " + ChatColor.DARK_GREEN +  staffLid + "'s " + ChatColor.GRAY + "hoofd op gedaan");
                        gui.close(p);
                    });
            gui.addItem(head);
        }
    }

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

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            ItemMeta item = event.getItem().getItemStack().getItemMeta();
            if (item.getPersistentDataContainer().has(plugin.uuidKey, PersistentDataType.STRING)) {
                String stringUUID = item.getPersistentDataContainer().get(plugin.uuidKey, PersistentDataType.STRING);

                Player player = (Player) event.getEntity();
                Document zoekDocument = new Document("UUID", player.getUniqueId().toString());
                Document doelDocument = (Document) plugin.getPlayers().find(zoekDocument).first();

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
                Document document = (Document) plugin.getStaffHeads().find(playerDocument).first();

                if (document != null) {
                    int uitgegevenHeads = document.getInteger("Uitgegeven heads");
                    document.put("Uitgegeven heads", uitgegevenHeads + 1);
                    plugin.getStaffHeads().replaceOne(playerDocument, document);
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
                plugin.getPlayers().updateOne(spelerDocument, spelerGevondenStaff);
            }
        }
    }
}
