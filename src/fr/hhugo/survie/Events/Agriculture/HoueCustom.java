package fr.hhugo.survie.Events.Agriculture;

import fr.hhugo.survie.Configurations.MessagesConfig;
import fr.hhugo.survie.Configurations.SurvieConfig;
import fr.hhugo.survie.Database.DatabaseManager;
import fr.hhugo.survie.Survie;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HoueCustom implements Listener, CommandExecutor
{
    private static final Survie plugin = Survie.getInstance();
    private static final MessagesConfig mc = MessagesConfig.getInstance();
    private static final SurvieConfig sc = SurvieConfig.getInstance();
    private static final DatabaseManager db = DatabaseManager.getInstance();

    private static final Map<String, String> replacements = Survie.replacements;

    private final Map<Material, Material> cultureTypes = Map.of(
            Material.WHEAT_SEEDS, Material.WHEAT,
            Material.CARROT, Material.CARROTS,
            Material.POTATO, Material.POTATOES,
            Material.BEETROOT_SEEDS, Material.BEETROOTS,
            Material.MELON_SEEDS, Material.MELON_STEM,
            Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM
    );

    private ItemStack creerHouePlantations()
    {
        ItemStack houe = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = houe.getItemMeta();
        if(meta != null)
        {
            String houeName = sc.getString("survie.agriculture.houe_plantations.name", replacements);
            meta.setDisplayName(houeName);
            List<String> houeLore = sc.getStringList("survie.agriculture.houe_plantations.lore", replacements);
            meta.setLore(houeLore);
            meta.setUnbreakable(true);
            houe.setItemMeta(meta);
        }

        houe.setAmount(1);

        return houe;
    }

    @EventHandler
    public void onHoueCustomInteract(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        ItemStack itemMain = player.getInventory().getItemInMainHand();
        if(isCustomHoue(itemMain))
        {
            if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;

            Block block = e.getClickedBlock();
            if(block != null)
            {
                planterGraines(player, block);
                //reduireDurabilite(player, itemMain);
            }
        }
    }

    private boolean isCustomHoue(ItemStack customHoue)
    {
        if (customHoue == null || customHoue.getType() != Material.GOLDEN_HOE)
        {
            return false;
        }

        ItemMeta meta = customHoue.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore())
        {
            return false;
        }

        String houeName = sc.getString("survie.agriculture.houe_plantations.name", replacements);
        if (!meta.getDisplayName().equals(houeName))
        {
            return false;
        }

        List<String> itemLore = meta.getLore();
        List<String> houeLore = sc.getStringList("survie.agriculture.houe_plantations.lore", replacements);
        if (itemLore == null || itemLore.size() != houeLore.size())
        {
            return false;
        }
        /*
        for (int i = 0; i < houeLore.size(); i++) {
            if (!houeLore.get(i).equals(itemLore.get(i)))
            {
                return false;
            }
        }
        */
        return true;
    }

    private void planterGraines(Player player, Block block)
    {
        for(int x = -1; x <= 1; x++)
        {
            for(int z = -1; z <= 1; z++)
            {
                Block blockCible = block.getRelative(x, 0, z);
                if(blockCible.getType() == Material.FARMLAND &&
                        blockCible.getRelative(0, 1, 0).getType() == Material.AIR)
                {
                    Material cultureType = getCulturesJoueur(player);
                    if(cultureType != null)
                    {
                        Material graine = cultureTypes.get(cultureType);
                        if(graine != null)
                        {
                            blockCible.getRelative(0, 1, 0).setType(graine);
                            removeCultureInventaire(player, cultureType);
                        }
                    }
                }
            }
        }
    }

    private Material getCulturesJoueur(Player player)
    {
        for(Material crop : cultureTypes.keySet())
        {
            if(player.getInventory().contains(crop))
                return crop;
        }
        return null;
    }

    private void removeCultureInventaire(Player player, Material cultureType)
    {
        ItemStack[] inventaire = player.getInventory().getContents();
        for(ItemStack item : inventaire)
        {
            if(item == null) continue;
            if(item.getType() == cultureType)
            {
                item.setAmount(item.getAmount() - 1);
                if(item.getAmount() <= 0)
                    player.getInventory().remove(item);
                return;
            }
        }
    }

    private void reduireDurabilite(Player player, ItemStack customHoue)
    {
        ItemMeta meta = customHoue.getItemMeta();
        if(meta == null) return;
        List<String> houeLore = meta.getLore();
        if(houeLore == null || houeLore.isEmpty()) return;

        for(int i = 0; i < houeLore.size(); i++)
        {
            String ligne = houeLore.get(i);
            if(ligne.contains("%amount%"))
            {
                int utilRestantes = Integer.parseInt(ligne.replaceAll("\\D+", ""));
                utilRestantes --;

                if(utilRestantes <= 0)
                {
                    player.getInventory().remove(customHoue);
                    return;
                }

                replacements.put("%amount%", String.valueOf(utilRestantes));
                houeLore = sc.getStringList("survie.agriculture.houe_plantations.lore", replacements);
                meta.setLore(houeLore);
            }
        }

        customHoue.setItemMeta(meta);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args)
    {
        replacements.put("%command%", cmd.getName());
        if(!(sender instanceof Player player))
        {
            sender.sendMessage(mc.getString("survie.message_erreur.non_joueur", replacements));
            return true;
        }

        player.getInventory().addItem(creerHouePlantations());
        player.sendMessage("Tu as reçu la houe custom!");

        return false;
    }
}
