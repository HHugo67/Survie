package fr.hhugo.survie.Events.Sante;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Objects;

public class Fracture implements Listener
{
    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    private BukkitTask tacheInfection;

    private static final Fracture INSTANCE = new Fracture();

    @EventHandler
    public void onFallDamage(EntityDamageEvent e)
    {
        if(e.getEntity() instanceof Player player)
        {
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL)
            {
                double chuteDistance = player.getFallDistance();
                double chuteDegats = calculateFallDamage(player, chuteDistance);
                if(!player.hasMetadata("AdminMode") ||
                        (player.hasMetadata("AdminMode") && !player.getMetadata("AdminMode").isEmpty()) &&
                                !player.getMetadata("AdminMode").get(0).asBoolean())
                {
                    if((chuteDistance >= 10.0 && chuteDegats >= 7.0) && !player.hasMetadata("Fracture"))
                    {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                                PotionEffect.INFINITE_DURATION, 1));
                        String fractureMessage = mc.getString("survie.sante.fracture.blessure", replacements);
                        player.sendMessage(fractureMessage);
                        player.setMetadata("Fracture", new FixedMetadataValue(plugin, true));

                        tacheInfection = Bukkit.getScheduler().runTaskLater(plugin, () -> infectPlayer(player), 20L * 60L);
                    }
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

        if(player.hasMetadata("Fracture") && material == Material.MILK_BUCKET)
        {
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1)), 0L);

            if(player.hasMetadata("Fracture"))
            {
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.addPotionEffect(
                        new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 0)), 0L);
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.addPotionEffect(
                        new PotionEffect(PotionEffectType.POISON, PotionEffect.INFINITE_DURATION, 0)), 0L);
            }
        }

        if(player.hasMetadata("Infection") && material == Material.GOLDEN_APPLE)
        {
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.POISON);
            player.removeMetadata("Infection", plugin);
            String guerisonMessage = mc.getString("survie.sante.infection.guerison", replacements);
            player.sendMessage(guerisonMessage);
            if(player.hasMetadata("Fracture"))
                tacheInfection = Bukkit.getScheduler().runTaskLater(plugin, () -> infectPlayer(player), 20L * 60L);
        }
    }

    public void craftBandage()
    {
        ItemStack bandage = new ItemStack(Material.PAPER, 1);
        ItemMeta bandageMeta = bandage.getItemMeta();
        Objects.requireNonNull(bandageMeta).setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Bandage");
        bandage.setItemMeta(bandageMeta);

        NamespacedKey bandageKey = new NamespacedKey(plugin, "Bandage");
        ShapedRecipe recipe = new ShapedRecipe(bandageKey, bandage);
        recipe.shape("S", "P", "S");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('P', Material.PAPER);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onInteractBandage(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if(item != null && item.getType() == Material.PAPER
                && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "" + ChatColor.BOLD + "Bandage"))
        {
            if(player.hasMetadata("Fracture"))
            {
                if(tacheInfection != null)
                {
                    tacheInfection.cancel();
                    tacheInfection = null;
                }
                player.removeMetadata("Fracture", plugin);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                String guerisonMessage = mc.getString("survie.sante.fracture.guerison", replacements);
                player.sendMessage(guerisonMessage);
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    private void infectPlayer(Player player)
    {
        plugin.getLogger().info("Joueur " + player.getName() + " infecté!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, PotionEffect.INFINITE_DURATION, 0));

        player.setMetadata("Infection", new FixedMetadataValue(plugin, true));

        String infectionMessage = mc.getString("survie.sante.infection.blessure", replacements);
        player.sendMessage(infectionMessage);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        Player player = e.getEntity();
        if (player.hasMetadata("Fracture") || player.hasMetadata("Infection")) {
            player.removeMetadata("Fracture", plugin);
            player.removeMetadata("Infection", plugin);
        }
    }

    public static Fracture getInstance()
    {
        return INSTANCE;
    }

}
