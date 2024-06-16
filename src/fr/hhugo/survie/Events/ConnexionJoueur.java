package fr.hhugo.survie.Events;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class ConnexionJoueur implements Listener
{
    private static final Survie plugin = Survie.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        boolean isAdmin = false;
        if(db.isAdmin(uuid) || player.isOp())
        {
            isAdmin = true;
            if(!player.hasMetadata("AdminMode"))
                player.setMetadata("AdminMode", new FixedMetadataValue(Survie.getInstance(), isAdmin));
            else if(player.getMetadata("AdminMode").isEmpty())
                player.setMetadata("AdminMode", new FixedMetadataValue(Survie.getInstance(), isAdmin));
        }

        db.addPlayer(uuid, player.getName(), isAdmin);

        if(sc.getBoolean("survie.connexion_joueur.join"))
        {
            replacements.put("%player%", player.getName());
            replacements.put("%current%", String.valueOf(Bukkit.getOnlinePlayers().size()));
            replacements.put("%max%", String.valueOf(Bukkit.getMaxPlayers()));
            String joinMessage = mc.getString("survie.connexion_joueur.join", replacements);
            e.setJoinMessage(joinMessage);
        }
        else
        {
            e.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        if(sc.getBoolean("survie.connexion_joueur.quit"))
        {
            Player player = e.getPlayer();
            replacements.put("%player%", player.getName());
            replacements.put("%current%", String.valueOf(Bukkit.getOnlinePlayers().size() - 1));
            replacements.put("%max%", String.valueOf(Bukkit.getMaxPlayers()));
            String joinMessage = mc.getString("survie.connexion_joueur.quit", replacements);
            e.setQuitMessage(joinMessage);
        }
        else
        {
            e.setQuitMessage(null);
        }
    }
}
