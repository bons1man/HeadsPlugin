package com.gio.heads.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.gio.heads.Heads;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Utils {

    private static Heads plugin;

    public Utils(Heads plugin) {
        this.plugin = plugin;
    }


    public static ItemStack createSkull(String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
        playerProfile.setProperty(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(playerProfile);
        skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Staff" + ChatColor.GRAY + "dex");
        skull.setItemMeta(skullMeta);

        return skull;
    }

    public static ItemStack createPlayerSkull(String name) {
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

    public static Document getPlayerDoucment(Player player) {
        Document spelerDocument = new Document("UUID", player.getUniqueId().toString());
        Document document = (Document) Heads.players.find(spelerDocument).first();

        if (document == null) {
            return null;
        }

        return document;
    }

    public static void addGuiItems(int staffListSize, List<Document> list, PaginatedGui gui) {
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

                        //todo: check hier de pdc inplaats van type
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
}
