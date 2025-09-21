package com.laoda.universalph;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DropsManager {
    private final UniversalPotatoHarvest plugin;
    private FileConfiguration dropsConfig;
    private File dropsFile;
    private final Map<String, List<CustomDrop>> dimensionDrops = new HashMap<>();
    private final Random random = new Random();

    public DropsManager(UniversalPotatoHarvest plugin) {
        this.plugin = plugin;
        loadDropsConfig();
    }

    /**
     * 加载掉落物配置文件
     */
    public void loadDropsConfig() {
        if (dropsFile == null) {
            dropsFile = new File(plugin.getDataFolder(), "drops.yml");
        }

        if (!dropsFile.exists()) {
            plugin.saveResource("drops.yml", false);
        }

        dropsConfig = YamlConfiguration.loadConfiguration(dropsFile);
        loadDrops();
    }

    /**
     * 保存掉落物配置到文件
     */
    public void saveDropsConfig() {
        if (dropsConfig == null || dropsFile == null) {
            return;
        }

        try {
            dropsConfig.save(dropsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存掉落物配置到 " + dropsFile, e);
        }
    }

    /**
     * 从配置文件加载所有维度的掉落物
     */
    private void loadDrops() {
        dimensionDrops.clear();

        // 获取配置文件中的所有维度
        for (String dimension : dropsConfig.getKeys(false)) {
            List<CustomDrop> drops = new ArrayList<>();
            ConfigurationSection dimensionSection = dropsConfig.getConfigurationSection(dimension);

            if (dimensionSection != null) {
                for (Map<?, ?> dropMap : dimensionSection.getMapList("")) {
                    CustomDrop drop = parseDropFromMap(dropMap);
                    if (drop != null) {
                        drops.add(drop);
                    }
                }
            } else {
                // 如果维度部分不是一个配置节，则尝试作为列表读取
                List<Map<?, ?>> dropsList = dropsConfig.getMapList(dimension);
                for (Map<?, ?> dropMap : dropsList) {
                    CustomDrop drop = parseDropFromMap(dropMap);
                    if (drop != null) {
                        drops.add(drop);
                    }
                }
            }

            dimensionDrops.put(dimension.toLowerCase(), drops);
        }

        plugin.getLogger().info("已加载 " + dimensionDrops.size() + " 个维度的掉落物配置");
    }

    /**
     * 从配置映射中解析单个掉落物
     */
    private CustomDrop parseDropFromMap(Map<?, ?> map) {
        try {
            String materialName = String.valueOf(map.get("material"));
            Material material = Material.matchMaterial(materialName);
            
            if (material == null) {
                plugin.getLogger().warning("无效的物品材质: " + materialName);
                return null;
            }

            double chance = 1.0;
            if (map.containsKey("chance")) {
                chance = Double.parseDouble(String.valueOf(map.get("chance")));
            }

            int minAmount = 1;
            int maxAmount = 1;
            if (map.containsKey("amount")) {
                String amountStr = String.valueOf(map.get("amount"));
                if (amountStr.contains("-")) {
                    String[] parts = amountStr.split("-");
                    minAmount = Integer.parseInt(parts[0]);
                    maxAmount = Integer.parseInt(parts[1]);
                } else {
                    minAmount = maxAmount = Integer.parseInt(amountStr);
                }
            }

            String displayName = map.containsKey("display-name") ? 
                String.valueOf(map.get("display-name")) : null;

            List<String> lore = new ArrayList<>();
            if (map.containsKey("lore")) {
                Object loreObj = map.get("lore");
                if (loreObj instanceof List) {
                    for (Object line : (List<?>) loreObj) {
                        lore.add(String.valueOf(line));
                    }
                }
            }

            Map<Enchantment, Integer> enchants = new HashMap<>();
            if (map.containsKey("enchants")) {
                Object enchantsObj = map.get("enchants");
                if (enchantsObj instanceof List) {
                    for (Object enchantObj : (List<?>) enchantsObj) {
                        String enchantStr = String.valueOf(enchantObj);
                        String[] parts = enchantStr.split(":");
                        if (parts.length == 2) {
                            Enchantment enchant = Enchantment.getByName(parts[0].toUpperCase());
                            int level = Integer.parseInt(parts[1]);
                            if (enchant != null) {
                                enchants.put(enchant, level);
                            }
                        }
                    }
                }
            }

            return new CustomDrop(material, chance, minAmount, maxAmount, displayName, lore, enchants);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "解析掉落物时出错", e);
            return null;
        }
    }

    /**
     * 根据世界获取随机掉落物
     * @param world 玩家所在的世界
     * @return 随机选择的掉落物，如果没有配置则返回默认掉落物
     */
    public ItemStack getRandomDrop(World world) {
        String worldType = getWorldType(world);
        List<CustomDrop> drops = dimensionDrops.get(worldType);

        // 如果没有为该维度配置掉落物，则使用默认掉落物
        if (drops == null || drops.isEmpty()) {
            return plugin.createCustomDrop();
        }

        // 根据概率选择掉落物
        double totalWeight = 0;
        for (CustomDrop drop : drops) {
            totalWeight += drop.getChance();
        }

        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;

        for (CustomDrop drop : drops) {
            currentWeight += drop.getChance();
            if (randomValue <= currentWeight) {
                return drop.createItemStack();
            }
        }

        // 如果没有选中任何掉落物（理论上不应该发生），则返回默认掉落物
        return plugin.createCustomDrop();
    }

    /**
     * 获取世界类型（主世界、下界或末地）
     * @param world 世界对象
     * @return 世界类型的字符串表示
     */
    private String getWorldType(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "nether";
            case THE_END:
                return "the_end";
            case NORMAL:
            default:
                return "world";
        }
    }

    /**
     * 自定义掉落物类
     */
    public class CustomDrop {
        private final Material material;
        private final double chance;
        private final int minAmount;
        private final int maxAmount;
        private final String displayName;
        private final List<String> lore;
        private final Map<Enchantment, Integer> enchants;

        public CustomDrop(Material material, double chance, int minAmount, int maxAmount, 
                          String displayName, List<String> lore, Map<Enchantment, Integer> enchants) {
            this.material = material;
            this.chance = chance;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.displayName = displayName;
            this.lore = lore;
            this.enchants = enchants;
        }

        public Material getMaterial() {
            return material;
        }

        public double getChance() {
            return chance;
        }

        /**
         * 创建物品堆
         * @return 根据配置创建的物品堆
         */
        public ItemStack createItemStack() {
            // 计算随机数量
            int amount = minAmount;
            if (maxAmount > minAmount) {
                amount = minAmount + random.nextInt(maxAmount - minAmount + 1);
            }

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                // 设置显示名称
                if (displayName != null && !displayName.isEmpty()) {
                    meta.setDisplayName(displayName.replace('&', '§'));
                }

                // 设置Lore
                if (lore != null && !lore.isEmpty()) {
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(line.replace('&', '§'));
                    }
                    meta.setLore(coloredLore);
                }

                // 添加附魔
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }

                item.setItemMeta(meta);
            }

            return item;
        }
    }
}