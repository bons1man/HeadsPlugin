package com.gio.heads.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.gio.heads.Heads;
import net.md_5.bungee.api.ChatColor;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("heads")
@CommandPermission("heads.staff")
public class SpawnHeadCommand {

    private Heads plugin;

    public SpawnHeadCommand(Heads plugin) {
        this.plugin = plugin;
    }

    @Subcommand("spawn <skin>")
    public void headsCommand(Player player, @Named("skin") String skin) {
        //todo: check of de sender een speler is
        //Als staff zijn/haar naam veranderd??
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(ChatColor.RED + skin + "'s " + org.bukkit.ChatColor.LIGHT_PURPLE + "Staff" + org.bukkit.ChatColor.GRAY + "dex" + ChatColor.RED + " hoofd");
        OfflinePlayer speler = Bukkit.getOfflinePlayer(skin);
        PlayerProfile spelerProfile = speler.getPlayerProfile();

        try {
            spelerProfile.complete(true);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Kon speler niet laden");
        }

        skullMeta.setPlayerProfile(spelerProfile);

        PersistentDataContainer persistentDataContainer = skullMeta.getPersistentDataContainer();
        persistentDataContainer.set(plugin.uuidKey, PersistentDataType.STRING, speler.getUniqueId().toString());

        skull.setItemMeta(skullMeta);

        player.getInventory().addItem(skull);

        Document goalDocument = new Document("UUID", speler.getUniqueId().toString());
        Document searchDocument = (Document) Heads.staffHeads.find(goalDocument).first();

        if (searchDocument == null) {
            Document document = new Document();
            document.put("Stafflid", skin);
            document.put("UUID", speler.getUniqueId().toString());
            document.put("Uitgegeven heads", 0);
            Heads.staffHeads.insertOne(document);
        }
    }
}
