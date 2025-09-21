package com.laoda.universalph;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockBreakListener implements Listener {
    private final UniversalPotatoHarvest plugin;

    public BlockBreakListener(UniversalPotatoHarvest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 检查破坏的方块是否是马铃薯作物
        if (block.getType() != Material.POTATO) {
            return;
        }

        // 检查马铃薯是否完全成熟 (age = 7)
        // 在1.12.2中，方块数据包含作物的年龄信息
        if (block.getData() != 7) {
            return;
        }

        // 检查玩家是否持有带有必需Lore的物品
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = handItem.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }

        String requiredLore = plugin.getRequiredLore();
        boolean hasRequiredLore = false;

        // 检查所有Lore行以找到必需的文本
        for (String line : meta.getLore()) {
            if (line.contains(requiredLore)) {
                hasRequiredLore = true;
                break;
            }
        }

        if (!hasRequiredLore) {
            return;
        }

        // 所有条件满足 - 处理自定义掉落
        event.setDropItems(false); // 取消原版掉落

        // 获取玩家所在世界
        World world = player.getWorld();
        
        // 从掉落物管理器获取基于维度的随机掉落物
        ItemStack customDrop = plugin.getDropsManager().getRandomDrop(world);

        // 如果工具上有时运附魔，则应用其效果
        int fortuneLevel = handItem.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        if (fortuneLevel > 0) {
            // 简单的时运计算: 掉落数量乘以 (时运等级 + 1)
            int extraDrops = fortuneLevel + 1;
            int newAmount = customDrop.getAmount() * extraDrops;
            customDrop.setAmount(Math.min(newAmount, customDrop.getMaxStackSize()));
        }

        // 在世界中掉落自定义物品
        block.getWorld().dropItemNaturally(block.getLocation(), customDrop);
        
        // 如果玩家有权限，显示调试信息
        if (player.hasPermission("universalpotatoharvest.debug")) {
            String worldType = getWorldTypeName(world);
            player.sendMessage("§7[UPH] §e在 " + worldType + " 维度收获了特殊土豆!");
        }
    }
    
    /**
     * 获取世界类型的友好名称
     * @param world 世界对象
     * @return 世界类型的友好名称
     */
    private String getWorldTypeName(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "下界";
            case THE_END:
                return "末地";
            case NORMAL:
            default:
                return "主世界";
        }
    }
}