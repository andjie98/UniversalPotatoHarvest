package com.laoda.universalph;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 检查破坏的方块是否是马铃薯作物
        if (block.getType() != Material.POTATOES) {
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

        String requiredLore = UniversalPotatoHarvest.getInstance().getRequiredLore();
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

        // 创建自定义掉落物品
        ItemStack customDrop = UniversalPotatoHarvest.getInstance().createCustomDrop();

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
    }
}