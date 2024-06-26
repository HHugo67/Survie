package fr.hhugo.survie.Events.Agriculture;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CulturesInteract implements Listener
{
    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    @EventHandler
    public void onHoueInteract(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        ItemStack itemMain = player.getInventory().getItemInMainHand();
        if(isHoue(itemMain))
        {
            if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND)
            {
                Block block = e.getClickedBlock();
                if(block != null && isCulture(block))
                {
                    Ageable ageable = (Ageable) block.getBlockData();
                    int age = ageable.getAge();
                    int maxAge = ageable.getMaximumAge();

                    double pourcentageCroissance = ((double) age / maxAge) * 100;
                    int pourcentageArrondi = (int) Math.round(pourcentageCroissance);

                    String croissanceMessage = ChatColor.GREEN + "" + pourcentageArrondi + "%";
                    plugin.sendActionBar(player, croissanceMessage);
                }
            }
        }
    }

    private boolean isHoue(ItemStack itemMain)
    {
        Material material = itemMain.getType();
        return switch (material)
        {
            case WOODEN_HOE, STONE_HOE, GOLDEN_HOE, IRON_HOE, DIAMOND_HOE, NETHERITE_HOE -> true;
            default -> false;
        };
    }

    private boolean isCulture(Block block)
    {
        Material material = block.getType();
        return switch (material)
        {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, MELON_STEM, PUMPKIN_STEM -> true;
            default -> false;
        };
    }
}
