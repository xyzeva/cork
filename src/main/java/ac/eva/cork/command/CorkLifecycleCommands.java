package ac.eva.cork.command;

import ac.eva.cork.CorkPlugin;
import ac.eva.cork.CorkStyling;
import ac.eva.cork.reflection.BukkitJavaPluginReflection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public class CorkLifecycleCommands {

    private final CorkPlugin plugin;

    @Command("cork load <file>")
    @Permission("cork.load")
    public void corkLoad(CommandSender sender, @Greedy @Argument(value = "file", suggestions = "pluginFiles") String fileName) {
        File pluginFile = this.plugin.getPluginsDirectory().resolve(fileName).toFile();
        if (!pluginFile.exists() || !pluginFile.isFile()) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin file doesn't exist")));
            return;
        }

        loadPluginWithFeedback(sender, pluginFile);
    }

    @Command("cork unload <plugin>")
    @Permission("cork.unload")
    public void corkUnload(CommandSender sender, @Argument(value = "plugin", suggestions = "plugins") String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin doesn't exist or isn't loaded")));
            return;
        }

        if (targetPlugin == this.plugin) {
            sender.sendMessage(CorkStyling.PREFIX
                    .append(Component.text("sadly, the jvm doesn't like cork doing actions to itself. sorry!")));
            return;
        }

        Bukkit.getGlobalRegionScheduler().run(this.plugin, $ -> {
            try {
                this.plugin.unloadPlugin(targetPlugin);
                sender.sendMessage(CorkStyling.PREFIX
                        .append(Component.text("disabled and unloaded plugin"))
                        .appendSpace()
                        .append(Component.text(targetPlugin.getName(), CorkStyling.PRIMARY_COLOR)));
            } catch (Exception ex) {
                sender.sendMessage(CorkStyling.PREFIX
                        .append(Component.text("failed to unload plugin: "))
                        .append(Component.text(ex.getMessage()))
                );
            }
        });
    }

    @Command("cork reload <plugin>")
    @Permission("cork.reload")
    public void corkReload(CommandSender sender, @Argument(value = "plugin", suggestions = "plugins") String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            sender.sendMessage(CorkStyling.PREFIX
                    .append(Component.text("failed to find plugin to reload, is the plugin not already loaded? use /cork load <file> for that.")));
            return;
        }

        if (targetPlugin == this.plugin) {
            sender.sendMessage(CorkStyling.PREFIX
                    .append(Component.text("sadly, the jvm doesn't like cork doing actions to itself. sorry!")));
            return;
        }

        if (!(targetPlugin instanceof JavaPlugin targetJavaPlugin)) {
            log.warn("plugin wasn't a java plugin, not sure how this happened (class={})", targetPlugin.getClass());
            sender.sendMessage(CorkStyling.PREFIX
                    .append(Component.text("plugin isn't a java plugin? check your console for more details.")));
            return;
        }
        File pluginFile = BukkitJavaPluginReflection.getPluginFile(targetJavaPlugin);

        Bukkit.getGlobalRegionScheduler().run(this.plugin, $ -> {
            try {
                this.plugin.unloadPlugin(targetPlugin);
                sender.sendMessage(CorkStyling.PREFIX
                        .append(Component.text("disabled and unloaded plugin"))
                        .appendSpace()
                        .append(Component.text(targetPlugin.getName(), CorkStyling.PRIMARY_COLOR)));
            } catch (Exception ex) {
                sender.sendMessage(CorkStyling.PREFIX
                        .append(Component.text("failed to unload plugin: "))
                        .append(Component.text(ex.getMessage()))
                );
                return;
            }

            loadPluginWithFeedback(sender, pluginFile);
        });
    }

    // todo: figure out why these dont work.
    // it appears PaperPluginInstanceManager closes classloaders and unregisters them from the pools, causing some weird behavior:
    // 1. the plugin gets enabled in "unsafe" mode with the global scope classloader
    // 2. lifecycle manager calls freak out because they have already been initialized once
    // 3. (maybe) some classloader might be left somewhere?
    // realistically this should be fine as most people can just use unload to properly clean stuff up.
    /*
    @Command("cork enable <plugin>")
    @Permission("cork.enable")
    public void corkEnable(CommandSender sender, @Argument(value = "plugin", suggestions = "plugins") String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin doesn't exist or isn't loaded")));
            return;
        }

        if (targetPlugin == this.plugin) {
            sender.sendMessage(CorkStyling.PREFIX
                    .append(Component.text("sadly, the jvm doesn't like cork doing actions to itself. sorry!")));
            return;
        }


        if (targetPlugin.isEnabled()) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin is already enabled")));
            return;
        }

        Bukkit.getPluginManager().enablePlugin(targetPlugin);
        sender.sendMessage(CorkStyling.PREFIX
                .append(Component.text("enabled plugin"))
                .appendSpace()
                .append(Component.text(targetPlugin.getName(), CorkStyling.PRIMARY_COLOR)));
    }

    @Command("cork disable <plugin>")
    @Permission("cork.disable")
    public void corkDisable(CommandSender sender, @Argument(value = "plugin", suggestions = "plugins") String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin doesn't exist or isn't loaded")));
            return;
        }

        if (targetPlugin == this.plugin) {
            sender.sendMessage(CorkStyling.PREFIX
                    .append(Component.text("sadly, the jvm doesn't like cork doing actions to itself. sorry!")));
            return;
        }

        if (!targetPlugin.isEnabled()) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin is already disabled")));
            return;
        }

        Bukkit.getPluginManager().disablePlugin(targetPlugin);
        sender.sendMessage(CorkStyling.PREFIX
                .append(Component.text("disabled plugin"))
                .appendSpace()
                .append(Component.text(targetPlugin.getName(), CorkStyling.PRIMARY_COLOR)));
    }
     */

    private void loadPluginWithFeedback(CommandSender sender, File pluginFile) {
        Bukkit.getGlobalRegionScheduler().run(this.plugin, $ -> {
            try {
                Plugin loadedPlugin = this.plugin.loadPlugin(pluginFile.toPath());
                sender.sendMessage(CorkStyling.PREFIX
                        .append(Component.text("loaded and enabled plugin"))
                        .appendSpace()
                        .append(Component.text(loadedPlugin.getName(), CorkStyling.PRIMARY_COLOR)));
            } catch (Exception ex) {
                sender.sendMessage(CorkStyling.PREFIX
                        .append(Component.text("failed to load plugin: "))
                        .append(Component.text(ex.getMessage()))
                );
            }
        });
    }
}
