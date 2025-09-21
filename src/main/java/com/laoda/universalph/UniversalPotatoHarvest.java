package com.laoda.universalph;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class UniversalPotatoHarvest extends JavaPlugin {
    private static UniversalPotatoHarvest instance;
    private FileConfiguration config;
    private DropsManager dropsManager;
    public static final Logger LOGGER = Logger.getLogger("UniversalPotatoHarvest");

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();
        
        // 初始化掉落物管理器
        dropsManager = new DropsManager(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);

        // 注册命令
        registerCommands();

        LOGGER.info("UniversalPotatoHarvest 插件已成功启用!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("UniversalPotatoHarvest 插件已禁用!");
    }

    /**
     * 注册插件命令
     */
    private void registerCommands() {
        CommandHandler commandHandler = new CommandHandler();
        getCommand("uph").setExecutor(commandHandler);
        getCommand("uph").setTabCompleter(commandHandler);
    }

    public static UniversalPotatoHarvest getInstance() {
        return instance;
    }

    /**
     * 获取掉落物管理器
     * @return 掉落物管理器实例
     */
    public DropsManager getDropsManager() {
        return dropsManager;
    }

    /**
     * 根据配置创建并返回自定义掉落物品（兼容旧版本）
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
            String displayName = config.getString("drop-item.display-name");
            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(displayName.replace('&', '§'));
            }

            // 设置自定义Lore（支持颜色代码）
            List<String> loreConfig = config.getStringList("drop-item.lore");
            if (loreConfig != null && !loreConfig.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : loreConfig) {
                    lore.add(line.replace('&', '§'));
                }
                meta.setLore(lore);
            }

            // 从配置添加附魔
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
        dropsManager.loadDropsConfig();
        LOGGER.info("配置已重载!");
    }

    /**
     * 创建一个带有指定Lore的收割工具
     * @param material 工具材质
     * @return 带有特定Lore的工具
     */
    public ItemStack createHarvestTool(Material material) {
        if (material == null) {
            material = Material.IRON_HOE;
        }
        
        ItemStack tool = new ItemStack(material);
        ItemMeta meta = tool.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6土豆收割工具");
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + getRequiredLore());
            lore.add("§e用于收获特殊土豆");
            
            meta.setLore(lore);
            tool.setItemMeta(meta);
        }
        
        return tool;
    }

    /**
     * 命令处理器类
     */
    private class CommandHandler implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                sendHelpMessage(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    if (!sender.hasPermission("universalpotatoharvest.admin")) {
                        sender.sendMessage("§c你没有权限执行此命令!");
                        return true;
                    }
                    reloadPluginConfig();
                    sender.sendMessage("§aUniversalPotatoHarvest 配置已重载!");
                    return true;
                    
                case "tool":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§c此命令只能由玩家执行!");
                        return true;
                    }
                    
                    if (!sender.hasPermission("universalpotatoharvest.tool")) {
                        sender.sendMessage("§c你没有权限执行此命令!");
                        return true;
                    }
                    
                    Material toolMaterial = Material.IRON_HOE;
                    if (args.length > 1) {
                        Material material = Material.matchMaterial(args[1]);
                        if (material != null) {
                            toolMaterial = material;
                        }
                    }
                    
                    Player player = (Player) sender;
                    player.getInventory().addItem(createHarvestTool(toolMaterial));
                    player.sendMessage("§a你获得了一个土豆收割工具!");
                    return true;
                    
                case "help":
                default:
                    sendHelpMessage(sender);
                    return true;
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                List<String> commands = Arrays.asList("reload", "tool", "help");
                
                for (String cmd : commands) {
                    if (cmd.startsWith(args[0].toLowerCase())) {
                        completions.add(cmd);
                    }
                }
                
                return completions;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("tool")) {
                List<String> completions = new ArrayList<>();
                List<String> materials = Arrays.asList(
                    "WOODEN_HOE", "STONE_HOE", "IRON_HOE", "GOLDEN_HOE", "DIAMOND_HOE",
                    "WOODEN_SHOVEL", "STONE_SHOVEL", "IRON_SHOVEL", "GOLDEN_SHOVEL", "DIAMOND_SHOVEL"
                );
                
                for (String material : materials) {
                    if (material.startsWith(args[1].toUpperCase())) {
                        completions.add(material);
                    }
                }
                
                return completions;
            }
            
            return null;
        }
        
        /**
         * 发送帮助信息
         * @param sender 命令发送者
         */
        private void sendHelpMessage(CommandSender sender) {
            sender.sendMessage("§6===== UniversalPotatoHarvest 帮助 =====");
            sender.sendMessage("§e/uph reload §7- 重载插件配置");
            sender.sendMessage("§e/uph tool [材质] §7- 获取一个土豆收割工具");
            sender.sendMessage("§e/uph help §7- 显示此帮助信息");
        }
    }
}