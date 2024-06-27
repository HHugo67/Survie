package fr.hhugo.survie.Commands.Coins;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoinsCommand implements CommandExecutor, TabCompleter
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

        replacements.put("%player%", player.getName());

        String uuid = player.getUniqueId().toString();

        if((db.isAdmin(uuid) || player.isOp()) && (player.hasMetadata("AdminMode") &&
                !player.getMetadata("AdminMode").isEmpty() && player.getMetadata("AdminMode").get(0).asBoolean()))
        {
            String usageMessage = "Usage: /coins <give/remove> <player> <amount>";

            if(args.length == 1)
            {
                Player cible = Bukkit.getPlayer(args[0]);
                if(cible == null)
                {
                    replacements.put("%target%", args[0]);
                    String mauvaiseCibleMessage = mc.getString("survie.message_erreur.joueur_inconnu", replacements);
                    player.sendMessage(mauvaiseCibleMessage);
                    return true;
                }

                String uuidCible = cible.getUniqueId().toString();
                String nameCible = cible.getName();
                replacements.put("%target%", nameCible);

                int coinsCible = db.getCoins(uuidCible);
                replacements.put("%coins%", String.valueOf(coinsCible));

                String getCoinsCibleMessage = mc.getString("survie.coins.player_amount", replacements);
                player.sendMessage(getCoinsCibleMessage);
                return false;
            }

            if(args.length == 2)
            {
                player.sendMessage(ChatColor.RED + usageMessage);
                return true;
            }

            if(args.length == 3)
            {
                Player cible = Bukkit.getPlayer(args[1]);
                if(cible == null)
                {
                    replacements.put("%target%", args[1]);
                    String mauvaiseCibleMessage = mc.getString("survie.message_erreur.joueur_inconnu", replacements);
                    player.sendMessage(mauvaiseCibleMessage);
                    return true;
                }

                String uuidCible = cible.getUniqueId().toString();
                String nameCible = cible.getName();
                replacements.put("%target%", nameCible);

                if(!plugin.isInteger(args[2]))
                {
                    player.sendMessage(ChatColor.RED + usageMessage);
                    return true;
                }

                int coins = Integer.parseInt(args[2]);
                switch(args[0])
                {
                    case "give":
                        replacements.put("%coins%", String.valueOf(coins));
                        int coinsGiveCible = db.addCoins(uuidCible, coins);
                        replacements.put("%amount%", String.valueOf(coinsGiveCible));

                        String addCoinsCibleMessage = mc.getString("survie.coins.player_give", replacements);
                        player.sendMessage(addCoinsCibleMessage);
                        String receiveCoinsMessage = mc.getString("survie.coins.receive", replacements);
                        cible.sendMessage(receiveCoinsMessage);
                        break;
                    case "remove":
                        if(coins > db.getCoins(uuidCible))
                            coins = db.getCoins(uuidCible);
                        replacements.put("%coins%", String.valueOf(coins));
                        int coinsRemoveCible = db.removeCoins(uuidCible, coins);
                        replacements.put("%amount%", String.valueOf(coinsRemoveCible));

                        String removeCoinsCibleMessage = mc.getString("survie.coins.player_remove", replacements);
                        player.sendMessage(removeCoinsCibleMessage);
                        String removeCoinsMessage = mc.getString("survie.coins.lose", replacements);
                        cible.sendMessage(removeCoinsMessage);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + usageMessage);
                        return true;
                }

                return false;
            }
        }

        if(args.length == 0)
        {
            int coins = db.getCoins(uuid);
            replacements.put("%amount%", String.valueOf(coins));
            String getCoinsMessage = mc.getString("survie.coins.amount", replacements);
            player.sendMessage(getCoinsMessage);
            return false;
        }

        String usageMessage = "Usage: /coins <pay> <player> <amount>";
        if(args.length < 3)
        {
            player.sendMessage(ChatColor.RED + usageMessage);
            return true;
        }

        if(args.length == 3 && args[0].equalsIgnoreCase("pay"))
        {
            Player cible = Bukkit.getPlayer(args[1]);
            if(cible == null)
            {
                replacements.put("%target%", args[1]);
                String mauvaiseCibleMessage = mc.getString("survie.message_erreur.joueur_inconnu", replacements);
                player.sendMessage(mauvaiseCibleMessage);
                return true;
            }

            String uuidCible = cible.getUniqueId().toString();
            String nameCible = cible.getName();
            replacements.put("%target%", nameCible);

            if(cible == player)
            {
                String yourselfMessage = mc.getString("survie.coins.yourself", replacements);
                player.sendMessage(yourselfMessage);
                return true;
            }

            if(!plugin.isInteger(args[2]))
            {
                player.sendMessage(ChatColor.RED + usageMessage);
                return true;
            }

            int coins = Integer.parseInt(args[2]);
            replacements.put("%coins%", String.valueOf(coins));

            if(coins > db.getCoins(uuid))
            {
                String notEnoughCoinsMessage = mc.getString("survie.coins.not_enough", replacements);
                player.sendMessage(notEnoughCoinsMessage);
                return true;
            }

            int coinsCible = db.addCoins(uuidCible, coins);
            replacements.put("%amount%", String.valueOf(coinsCible));
            db.removeCoins(uuid, coins);

            String sendCoinsMessage = mc.getString("survie.coins.player_give", replacements);
            player.sendMessage(sendCoinsMessage);
            String receiveCoinsMessage = mc.getString("survie.coins.receive", replacements);
            cible.sendMessage(receiveCoinsMessage);

            return false;
        }
        else
        {
            player.sendMessage(ChatColor.RED + usageMessage);
            return true;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String str, String[] args)
    {
        List<String> completer = new ArrayList<String>();

        if(!(sender instanceof Player player))
            return completer;

        String uuid = player.getUniqueId().toString();

        if((db.isAdmin(uuid) || player.isOp()) && (player.hasMetadata("AdminMode") &&
                !player.getMetadata("AdminMode").isEmpty() && player.getMetadata("AdminMode").get(0).asBoolean()))
        {
            if(args.length == 1)
            {
                completer.add("give");
                completer.add("remove");
                completer.add("pay");
                for(Player players : Bukkit.getOnlinePlayers())
                    completer.add(players.getName());
            }

            if(args.length == 2)
            {
                for(Player players : Bukkit.getOnlinePlayers())
                    completer.add(players.getName());
            }
            return completer;
        }

        if(args.length == 1)
            completer.add("pay");

        if(args.length == 2)
        {
            for(Player players : Bukkit.getOnlinePlayers())
            {
                if(players == player) continue;
                completer.add(players.getName());
            }
        }

        return completer;
    }
}
