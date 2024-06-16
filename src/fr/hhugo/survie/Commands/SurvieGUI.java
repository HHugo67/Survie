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

import java.util.List;
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
            setItemInInventory(inventaire,0,
                    sc.getMaterial("survie.gui.join_message_item.material"),
                    sc.getString("survie.gui.join_message_item.name", replacements),
                    sc.getStringList("survie.gui.join_message_item.lore", replacements));
            // endregion

            // region Item message de quit
            replacements.put("%state%", String.valueOf(sc.getBoolean("survie.connexion_joueur.quit")));
            setItemInInventory(inventaire, 1,
                    sc.getMaterial("survie.gui.quit_message_item.material"),
                    sc.getString("survie.gui.quit_message_item.name", replacements),
                    sc.getStringList("survie.gui.quit_message_item.lore", replacements));
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

            // region Item message de quit
            if(e.getSlot() == 1)
            {
                ItemStack quitMessageStack = e.getCurrentItem();
                ItemMeta quitMessageMeta = quitMessageStack.getItemMeta();
                if(sc.getBoolean("survie.connexion_joueur.quit"))
                    sc.set("survie.connexion_joueur.quit", false);
                else
                    sc.set("survie.connexion_joueur.quit", true);
                replacements.put("%state%", String.valueOf(sc.getBoolean("survie.connexion_joueur.quit")));
                quitMessageMeta.setLore(sc.getStringList("survie.gui.quit_message_item.lore", replacements));
                quitMessageStack.setItemMeta(quitMessageMeta);
            }
            // endregion
        }
        else if(inventoryView.getTitle().equalsIgnoreCase(sc.getString("survie.gui_joueur.name", replacements)))
        {
            e.setCancelled(true);
        }
    }

    private void setItemInInventory(Inventory inventaire,  int slot, Material material, String name, List<String> lore)
    {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null)
        {
            itemMeta.setDisplayName(name);
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        inventaire.setItem(slot, itemStack);
    }
}
