package fr.hhugo.survie.Events.Sante;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class Fracture implements Listener
{
    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    @EventHandler
    public void onFallDamage(EntityDamageEvent e)
    {
        if(e.getEntity() instanceof Player player)
        {
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL)
            {
                double chuteDistance = player.getFallDistance();
                double chuteDegats = calculateFallDamage(player, chuteDistance);
                player.sendMessage(ChatColor.BLUE + "" + chuteDistance);
                player.sendMessage(ChatColor.RED + "" + chuteDegats);
                if(chuteDistance >= 10.0 && chuteDegats >= 7.0)
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                            PotionEffect.INFINITE_DURATION, 1));
                    String fractureMessage = mc.getString("survie.sante.fracture", replacements);
                    player.sendMessage(fractureMessage);
                    player.setMetadata("Fracture", new FixedMetadataValue(plugin, true));
                }
            }
        }
    }

    private double calculateFallDamage(Player player, double chuteDistance)
    {
        double degats = chuteDistance - 3;
        if(degats <= 0.0)
            return 0.0;
        double reductionDegats = 0.0;
        if(player.getInventory().getBoots() != null
                && player.getInventory().getBoots().containsEnchantment(Enchantment.FEATHER_FALLING))
        {
            int niveau = player.getInventory().getBoots().getEnchantmentLevel(Enchantment.FEATHER_FALLING);
            reductionDegats = 0.12 * niveau;
        }

        degats *= (1 - reductionDegats);

        return degats;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e)
    {
        Player player = e.getPlayer();
        Material material = e.getItem().getType();

        if(player.hasMetadata("Fracture") && player.getMetadata("Fracture").get(0).asBoolean()
                && (material == Material.MILK_BUCKET))
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1)), 1L);
    }

    
}
