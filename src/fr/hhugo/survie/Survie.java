package fr.hhugo.survie;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Survie extends JavaPlugin
{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    @Override
    public void onEnable()
    {
        getLogger().info(ANSI_WHITE_BACKGROUND + ANSI_GREEN + "Le plugin de Survie est actif" + ANSI_RESET);
    }

    @Override
    public void onDisable()
    {
        getLogger().info(ANSI_WHITE_BACKGROUND + ANSI_RED + "Le plugin de survie n'est plus actif" + ANSI_RESET);
    }

    public static Survie getInstance()
    {
        return getPlugin(Survie.class);
    }
}
