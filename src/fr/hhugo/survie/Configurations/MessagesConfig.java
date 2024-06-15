package fr.hhugo.survie.Configurations;

import fr.hhugo.survie.Survie;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessagesConfig
{
    private final static Survie plugin = Survie.getInstance();
    private final static MessagesConfig INSTANCE = new MessagesConfig();

    private File file;
    private YamlConfiguration config;

    private MessagesConfig() {}

    public void load()
    {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if(!file.exists())
        {
            plugin.getLogger().info(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_GREEN
                    + "Création du fichier messages.yml..." + Survie.ANSI_RESET);
            plugin.saveResource("messages.yml", false);
        }

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try
        {
            config.load(file);
        }
        catch (Exception ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }
    }

    public void save()
    {
        try
        {
            config.save(file);
        }
        catch (Exception ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }
    }

    public void set(String path, Object value)
    {
        config.set(path, value);
        save();
    }

    public boolean getBoolean(String path)
    {
        return config.getBoolean(path);
    }

    public int getInt(String path)
    {
        return config.getInt(path);
    }

    public double getDouble(String path)
    {
        return config.getDouble(path);
    }

    public String getString(String path)
    {
        return config.getString(path);
    }

    public String getString(String path, Map<String, String> replacements)
    {
        String value = getString(path);
        StringBuilder sb = new StringBuilder(value);

        for(Map.Entry<String, String> entry : replacements.entrySet())
        {
            String target = entry.getKey();
            String replacement = entry.getValue();
            int index = sb.indexOf(target);
            while(index != -1)
            {
                sb.replace(index, index + target.length(), replacement);
                index = sb.indexOf(target, index + replacement.length());
            }
        }

        return sb.toString();
    }

    public List<String> getStringList(String path)
    {
        return config.getStringList(path);
    }

    public List<String> getStringList(String path, Map<String, String> replacements)
    {
        List<String> list = getStringList(path);
        List<String> finalList = new ArrayList<>();
        for(String str : list)
        {
            StringBuilder sb = new StringBuilder(str);
            for(Map.Entry<String, String> entry : replacements.entrySet())
            {
                String target = entry.getKey();
                String replacement = entry.getValue();
                int index = sb.indexOf(target);
                while(index != -1)
                {
                    sb.replace(index, index + target.length(), replacement);
                    index = sb.indexOf(target, index + replacement.length());
                }
            }
            finalList.add(sb.toString());
        }
        return finalList;
    }

    public ConfigurationSection getConfigurationSection(String path)
    {
        return config.getConfigurationSection(path);
    }

    public static MessagesConfig getInstance()
    {
        return INSTANCE;
    }
}
