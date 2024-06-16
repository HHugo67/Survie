package fr.hhugo.survie.Commands;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class AdminCommand implements CommandExecutor
{

    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args)
    {
        replacements.put("%command%", cmd.getName());
        if(!(sender instanceof Player player))
        {
            sender.sendMessage(mc.getString("survie.message_erreur.non_joueur", replacements));
            return true;
        }

        if(player.hasPermission("survie.admin") || player.hasPermission("survie.*") || player.isOp())
        {
            if(player.hasMetadata("AdminMode"))
            {
                if(player.getMetadata("AdminMode").get(0).asBoolean())
                    player.setMetadata("AdminMode", new FixedMetadataValue(plugin, false));
                else
                    player.setMetadata("AdminMode", new FixedMetadataValue(plugin, true));
            }
            else
                player.setMetadata("AdminMode", new FixedMetadataValue(plugin, true));

            replacements.put("%adminmode%", String.valueOf(player.getMetadata("AdminMode").get(0).asBoolean()));
            player.sendMessage(mc.getString("survie.admin_mode", replacements));
            return false;
        }

        player.sendMessage(mc.getString("survie.message_erreur.non_permission", replacements));

        return true;
    }
}
