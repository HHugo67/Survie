package fr.hhugo.survie.Events.Chat;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;

public class ChatListener implements Listener
{

    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e)
    {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        String prefix;
        if(db.isAdmin(uuid))
            prefix = mc.getString("survie.rang.admin.prefix", replacements) + " ";
        else
            prefix = mc.getString("survie.rang.joueur.prefix", replacements) + " ";
        replacements.put("%rank%", prefix);
        replacements.put("%player%", player.getName());
        replacements.put("%message%", e.getMessage());

        String formatMessage = sc.getString("survie.chat.format", replacements);
        e.setFormat(formatMessage);
    }
}
