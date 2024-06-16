package fr.hhugo.survie;

import fr.hhugo.survie.Commands.AdminCommand;
import fr.hhugo.survie.Commands.GUI.SurvieGUI;
import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Events.ConnexionJoueur;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Survie extends JavaPlugin
{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static final Map<String, String> replacements = new HashMap<>();

    @Override
    public void onEnable()
    {
        SurvieConfig.getInstance().load();
        MessagesConfig.getInstance().load();

        replacements.put("&", "§");

        DatabaseManager db = DatabaseManager.getInstance();
        db.connect(SurvieConfig.getInstance().getString("survie.database"));

        registerEvents();
        registerCommands();

        getLogger().info(ANSI_WHITE_BACKGROUND + ANSI_GREEN + "Le plugin de Survie est actif" + ANSI_RESET);
    }

    @Override
    public void onDisable()
    {
        replacements.clear();

        DatabaseManager db = DatabaseManager.getInstance();
        db.disconnect();

        getLogger().info(ANSI_WHITE_BACKGROUND + ANSI_RED + "Le plugin de survie n'est plus actif" + ANSI_RESET);
    }

    private void registerEvents()
    {
        getServer().getPluginManager().registerEvents(new ConnexionJoueur(), this);

        getServer().getPluginManager().registerEvents(new SurvieGUI(), this);
    }

    private void registerCommands()
    {
        Objects.requireNonNull(getCommand("gui")).setExecutor(new SurvieGUI());
        Objects.requireNonNull(getCommand("admin")).setExecutor(new AdminCommand());
    }

    public static Survie getInstance()
    {
        return getPlugin(Survie.class);
    }
}
