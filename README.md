# UniversalPotatoHarvest

## 项目简介
UniversalPotatoHarvest（全维度土豆收获自定义掉落物）是一个 Minecraft 服务器插件，专为 Minecraft 1.12.2 版本开发。该插件允许服务器管理员自定义土豆作物收获时的掉落物，可能支持在不同维度（主世界、下界、末地等）设置不同的掉落配置。

## 功能特点
- 自定义土豆作物收获掉落物
- 支持多维度掉落物配置
- 可配置掉落概率、数量等参数
- 与 Paper/Spigot 1.12.2 完全兼容

## 安装方法
1. 从 [Releases](https://github.com/yourusername/UniversalPotatoHarvest/releases) 页面下载最新版本的插件 JAR 文件
2. 将下载的 JAR 文件放入服务器的 `plugins` 目录
3. 重启服务器或使用插件管理命令加载插件
4. 生成默认配置文件后，根据需要进行自定义设置

## 配置说明
插件首次运行后会在 `plugins/UniversalPotatoHarvest` 目录下生成配置文件：
- `config.yml` - 主配置文件，包含基本设置
- `drops.yml` - 掉落物配置文件，可自定义各维度的掉落物

### 配置示例
```yaml
# 示例配置，具体以实际生成的配置文件为准
drops:
  world:  # 主世界
    potato:
      - item: DIAMOND
        chance: 0.01
        amount: 1-2
      - item: GOLD_INGOT
        chance: 0.05
        amount: 1-3
  nether:  # 下界
    potato:
      - item: NETHER_STAR
        chance: 0.005
        amount: 1
```

## 命令与权限
- `/uph reload` - 重新加载配置文件
  - 权限: `universalpotatoharvest.admin`
- `/uph help` - 显示帮助信息
  - 权限: `universalpotatoharvest.use`

## 技术信息
- 开发语言: Java 8
- 构建工具: Maven
- 服务器API: Paper API 1.12.2-R0.1-SNAPSHOT

## 开发与构建
如果您想参与开发或自行构建插件：

1. 克隆仓库
```bash
git clone https://github.com/yourusername/UniversalPotatoHarvest.git
```

2. 使用 Maven 构建
```bash
cd UniversalPotatoHarvest
mvn clean package
```

3. 构建完成后，JAR 文件将位于 `target` 目录

## 依赖项
- Paper/Spigot 1.12.2
- Java 8 或更高版本

## 问题反馈
如果您在使用过程中遇到任何问题，或有功能建议，请通过以下方式反馈：
- [提交 Issue](https://github.com/yourusername/UniversalPotatoHarvest/issues)
- 联系邮箱: your.email@example.com

## 许可证
本项目采用 [MIT 许可证](LICENSE) 进行授权。

## 贡献者
- [您的名字](https://github.com/yourusername) - 项目创建者与维护者