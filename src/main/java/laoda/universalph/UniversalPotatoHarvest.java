package com.laoda.universalph;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class UniversalPotatoHarvest extends JavaPlugin {
    private static UniversalPotatoHarvest instance;
    private FileConfiguration config;
    public static final Logger LOGGER = Logger.getLogger("UniversalPotatoHarvest");

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);

        // 设置重载命令
        this.getCommand("uphreload").setExecutor((sender, command, label, args) -> {
            reloadPluginConfig();
            sender.sendMessage("§aUniversalPotatoHarvest 配置已重载!");
            return true;
        });

        LOGGER.info("UniversalPotatoHarvest 插件已成功启用!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("UniversalPotatoHarvest 插件已禁用!");
    }

    public static UniversalPotatoHarvest getInstance() {
        return instance;
    }

    /**
     * 根据配置创建并返回自定义掉落物品
     * @return 带有自定义名称、Lore和附魔的ItemStack
     */
    public ItemStack createCustomDrop() {
        // 从配置获取材质，如果没有则使用DIAMOND作为默认值
        Material material = Material.matchMaterial(config.getString("drop-item.material", "DIAMOND"));
        if (material == null) material = Material.DIAMOND;

        // 从配置获取数量，如果没有则使用1作为默认值
        int amount = config.getInt("drop-item.amount", 1);
        ItemStack customDrop = new ItemStack(material, amount);

        ItemMeta meta = customDrop.getItemMeta();
        if (meta != null) {
            // 设置自定义显示名称（支持颜色代码）
            // 例如: "&6末地土豆" 会显示为金色的"末地土豆"
            String displayName = config.getString("drop-item.display-name");
            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(displayName.replace('&', '§'));
            }

            // 设置自定义Lore（支持颜色代码）
            // 配置示例:
            // lore:
            // - "&7来自末地的特殊土豆"
            // - "&e蕴含神秘能量"
            List<String> loreConfig = config.getStringList("drop-item.lore");
            if (loreConfig != null && !loreConfig.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : loreConfig) {
                    lore.add(line.replace('&', '§'));
                }
                meta.setLore(lore);
            }

            // 从配置添加附魔
            // 格式: "附魔ID:等级"
            // 配置示例:
            // enchants:
            // - "DURABILITY:3"  # 耐久III
            // - "MENDING:1"     # 经验修补I
            List<String> enchants = config.getStringList("drop-item.enchants");
            for (String enchantStr : enchants) {
                String[] parts = enchantStr.split(":");
                if (parts.length == 2) {
                    try {
                        Enchantment enchant = Enchantment.getByName(parts[0].toUpperCase());
                        int level = Integer.parseInt(parts[1]);
                        if (enchant != null) {
                            meta.addEnchant(enchant, level, true);
                        }
                    } catch (NumberFormatException e) {
                        getLogger().warning("无效的附魔等级格式: " + enchantStr);
                    }
                } else {
                    getLogger().warning("无效的附魔格式: " + enchantStr + ". 请使用格式: 附魔ID:等级");
                }
            }

            customDrop.setItemMeta(meta);
        }
        return customDrop;
    }

    /**
     * 获取手持物品必须包含的Lore文本
     * @return 必需的Lore文本
     */
    public String getRequiredLore() {
        return config.getString("trigger-item.lore", "土豆收割者");
    }

    /**
     * 重载插件配置
     */
    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        LOGGER.info("配置已重载!");
    }
}