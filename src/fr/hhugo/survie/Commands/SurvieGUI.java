package fr.hhugo.survie.Commands;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public class SurvieGUI implements CommandExecutor, Listener
{
    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    private final int TAILLE_GUI = 9*6;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args)
    {
        replacements.put("%command%", cmd.getName());
        if(!(sender instanceof Player player))
        {
            sender.sendMessage(mc.getString("survie.message_erreur.non_joueur", replacements));
            return true;
        }

        replacements.put("%player%", player.getName());
        Inventory inventaire;

        if(player.hasPermission("survie.gui") || player.isOp())
        {
            inventaire = Bukkit.createInventory(player, TAILLE_GUI,
                    sc.getString("survie.gui.name", replacements));

            // region Item message de join
            replacements.put("%state%", String.valueOf(sc.getBoolean("survie.connexion_joueur.join")));
            ItemStack joinMessageStack = new ItemStack(Material.GREEN_DYE);
            ItemMeta joinMessageMeta = joinMessageStack.getItemMeta();
            if(joinMessageMeta != null)
            {
                joinMessageMeta.setDisplayName(sc.getString("survie.gui.join_message_item.name", replacements));
                joinMessageMeta.setLore(sc.getStringList("survie.gui.join_message_item.lore", replacements));
            }
            joinMessageStack.setItemMeta(joinMessageMeta);
            inventaire.setItem(0, joinMessageStack);
            // endregion
        }
        else
        {
            inventaire = Bukkit.createInventory(player, TAILLE_GUI,
                    sc.getString("survie.gui_joueur.name", replacements));
        }

        player.openInventory(inventaire);
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        InventoryView inventoryView = e.getView();
        if(inventoryView.getTitle().equalsIgnoreCase(sc.getString("survie.gui.name", replacements)))
        {
            e.setCancelled(true);

            // region Item message de join
            if(e.getSlot() == 0)
            {
                ItemStack joinMessageStack = e.getCurrentItem();
                ItemMeta joinMessageMeta = joinMessageStack.getItemMeta();
                if(sc.getBoolean("survie.connexion_joueur.join"))
                    sc.set("survie.connexion_joueur.join", false);
                else
                    sc.set("survie.connexion_joueur.join", true);
                replacements.put("%state%", String.valueOf(sc.getBoolean("survie.connexion_joueur.join")));
                joinMessageMeta.setLore(sc.getStringList("survie.gui.join_message_item.lore", replacements));
                joinMessageStack.setItemMeta(joinMessageMeta);
            }
            // endregion
        }
        else if(inventoryView.getTitle().equalsIgnoreCase(sc.getString("survie.gui_joueur.name", replacements)))
        {
            e.setCancelled(true);
        }
    }
}
