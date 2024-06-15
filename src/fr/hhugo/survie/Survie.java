package fr.hhugo.survie;

import org.bukkit.plugin.java.JavaPlugin;

public class Survie extends JavaPlugin
{

    private static final Survie INSTANCE = new Survie();

    @Override
    public void onEnable()
    {
        getLogger().info("Survie is enabled");
    }

    @Override
    public void onDisable()
    {
        getLogger().info("Survie is disabled");
    }

    public static Survie getInstance()
    {
        return INSTANCE;
    }
}
