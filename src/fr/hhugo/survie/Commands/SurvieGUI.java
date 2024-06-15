package fr.hhugo.survie.Commands;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class SurvieGUI implements CommandExecutor
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
            sender.sendMessage(mc.getString("survie.non_joueur", replacements));
            return true;
        }

        Inventory inventaire = Bukkit.createInventory(player, TAILLE_GUI,
                sc.getString("survie.gui.name", replacements));

        player.openInventory(inventaire);
        return false;
    }
}
