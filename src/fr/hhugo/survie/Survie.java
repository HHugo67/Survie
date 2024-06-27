package fr.hhugo.survie;

import fr.hhugo.survie.Commands.AdminCommand;
import fr.hhugo.survie.Commands.Coins.CoinsCommand;
import fr.hhugo.survie.Commands.GUI.SurvieGUI;
import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Events.Agriculture.CulturesInteract;
import fr.hhugo.survie.Events.Agriculture.HoueCustom;
import fr.hhugo.survie.Events.Chat.ChatListener;
import fr.hhugo.survie.Events.ConnexionJoueur;
import fr.hhugo.survie.Events.Sante.Fracture;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
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

        loadRecipes();
        registerEvents();
        registerCommands();
        setTabCompleter();
        runTasks();

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
        getServer().getPluginManager().registerEvents(new Fracture(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new CulturesInteract(), this);
        getServer().getPluginManager().registerEvents(new HoueCustom(), this);
    }

    private void registerCommands()
    {
        Objects.requireNonNull(getCommand("gui")).setExecutor(new SurvieGUI());
        Objects.requireNonNull(getCommand("admin")).setExecutor(new AdminCommand());
        Objects.requireNonNull(getCommand("customhoe")).setExecutor(new HoueCustom());
        Objects.requireNonNull(getCommand("coins")).setExecutor(new CoinsCommand());
    }

    private void setTabCompleter()
    {
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(new AdminCommand());
        Objects.requireNonNull(getCommand("coins")).setTabCompleter(new CoinsCommand());
    }

    private void runTasks()
    {

    }

    private void loadRecipes()
    {
        Fracture.getInstance().craftBandage();
    }

    public void sendActionBar(Player player, String message)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }

    public boolean isInteger(String str)
    {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Survie getInstance()
    {
        return getPlugin(Survie.class);
    }
}
