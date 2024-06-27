package fr.hhugo.survie.Commands;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminCommand implements CommandExecutor, TabCompleter {

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

        String uuid = player.getUniqueId().toString();
        if(db.isAdmin(uuid) || player.isOp())
        {
            if(args.length == 0)
            {
                setAdminMetadata(player);

                replacements.put("%adminmode%", String.valueOf(player.getMetadata("AdminMode").get(0).asBoolean()));
                String adminModeMessage = mc.getString("survie.rang.admin.mode", replacements);
                player.sendMessage(adminModeMessage);
                return false;
            }

            if(args.length == 2)
            {
                String adminPrefix = mc.getString("survie.rang.admin.prefix", replacements);
                replacements.put("%prefix%", adminPrefix);

                Player cible = Bukkit.getPlayer(args[1]);
                replacements.put("%target%", args[1]);
                String cibleUuid;
                if(cible == null)
                {
                    UUID cibleOfflineUuid = db.getPlayerUUID(args[1]);
                    if(cibleOfflineUuid == null)
                    {
                        String mauvaiseCibleMessage = mc.getString("survie.message_erreur.joueur_inconnu", replacements);
                        player.sendMessage(mauvaiseCibleMessage);
                        return true;
                    }
                    else
                    {
                        OfflinePlayer cibleOffline = Bukkit.getOfflinePlayer(cibleOfflineUuid);
                        cibleUuid = cibleOffline.getUniqueId().toString();
                    }
                }
                else
                    cibleUuid = cible.getUniqueId().toString();

                switch(args[0])
                {
                    case "add":
                        if(db.isAdmin(cibleUuid))
                        {
                            String dejaAdminMessage = mc.getString("survie.rang.admin.deja_admin", replacements);
                            player.sendMessage(dejaAdminMessage);
                            return false;
                        }

                        if(db.setAdmin(cibleUuid, true))
                        {
                            String cibleAdminMessage = mc.getString("survie.rang.admin.add", replacements);
                            String joueurAdminMessage = mc.getString("survie.rang.admin.joueur_add", replacements);
                            player.sendMessage(cibleAdminMessage);
                            if(cible != null)
                                cible.sendMessage(joueurAdminMessage);
                        }
                        else
                        {
                            Bukkit.broadcastMessage(ChatColor.RED + "Impossible de mettre " +
                                    ChatColor.GOLD + args[1] + ChatColor.RED + " administrateur");
                        }
                        return false;

                    case "remove":
                        if(!db.isAdmin(cibleUuid))
                        {
                            String dejaNonAdminMessage = mc.getString("survie.rang.admin.deja_non_admin", replacements);
                            player.sendMessage(dejaNonAdminMessage);
                            return false;
                        }

                        if(db.setAdmin(cibleUuid, false))
                        {
                            String cibleAdminMessage = mc.getString("survie.rang.admin.remove", replacements);
                            String joueurAdminMessage = mc.getString("survie.rang.admin.joueur_remove", replacements);
                            player.sendMessage(cibleAdminMessage);
                            if(cible != null)
                                cible.sendMessage(joueurAdminMessage);
                        }
                        else
                        {
                            Bukkit.broadcastMessage(ChatColor.RED + "Impossible d'enlever administrateur à " +
                                    ChatColor.GOLD + args[1]);
                        }
                        return false;


                    default:
                        player.sendMessage(ChatColor.RED + "Usage: /admin <add/remove> <joueur>");
                        return false;
                }
            }
            return false;
        }
        else
        {
            removeAdminMetadata(player);
            player.sendMessage(mc.getString("survie.message_erreur.non_permission", replacements));
        }

        return true;
    }


    private void setAdminMetadata(Player player)
    {
        if(db.isAdmin(player.getUniqueId().toString()) || player.isOp())
        {
            if(player.hasMetadata("AdminMode"))
            {
                if(player.getMetadata("AdminMode").isEmpty())
                    player.setMetadata("AdminMode", new FixedMetadataValue(Survie.getInstance(), true));

                if(player.getMetadata("AdminMode").get(0).asBoolean())
                    player.setMetadata("AdminMode", new FixedMetadataValue(Survie.getInstance(), false));
                else
                    player.setMetadata("AdminMode", new FixedMetadataValue(Survie.getInstance(), true));
            }
            else
                player.setMetadata("AdminMode", new FixedMetadataValue(Survie.getInstance(), true));
        }
    }

    private void removeAdminMetadata(Player player)
    {
        if(player.hasMetadata("AdminMode"))
            player.removeMetadata("AdminMode", plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String str, String[] args)
    {
        List<String> completer = new ArrayList<String>();

        if(!(sender instanceof Player player))
            return completer;

        String uuid = player.getUniqueId().toString();
        if(db.isAdmin(uuid) || player.isOp())
        {
            if(args.length == 1)
            {
                completer.add("add");
                completer.add("remove");
            }
            else if(args.length == 2)
            {
                if(args[0].equalsIgnoreCase("add"))
                {
                    for(Player players : Bukkit.getOnlinePlayers())
                    {
                        String playersUuid = players.getUniqueId().toString();
                        if(db.isAdmin(playersUuid))
                            continue;
                        completer.add(players.getName());
                    }
                }
                else if(args[0].equalsIgnoreCase("remove"))
                {
                    for(UUID uuids : db.getAllAdmins())
                    {
                        String playersUuid = uuids.toString();
                        String pseudo = db.getPlayerName(playersUuid);
                        if(!db.isAdmin(playersUuid))
                            continue;
                        completer.add(pseudo);
                    }
                }
            }
        }

        return completer;
    }
}
