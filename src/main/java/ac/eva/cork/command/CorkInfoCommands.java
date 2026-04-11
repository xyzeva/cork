package ac.eva.cork.command;

import ac.eva.cork.CorkStyling;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.APICommandMeta;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.bukkit.BukkitBrigForwardingMap;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CorkInfoCommands {
    @Command("cork info <plugin>")
    @Permission("cork.info")
    public void corkInfo(CommandSender sender, @Argument(value = "plugin", suggestions = "plugins") String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("plugin doesn't exist or isn't loaded")));
            return;
        }

        List<Component> infoLines = new ArrayList<>();
        PluginMeta pluginMeta = targetPlugin.getPluginMeta();
        infoLines.add(CorkStyling.PREFIX.append(Component.text("plugin"))
                .appendSpace()
                .append(Component.text(targetPlugin.getName(), CorkStyling.PRIMARY_COLOR))
                .appendSpace()
                .append(Component.text(pluginMeta.getVersion(), CorkStyling.SECONDARY_COLOR))
        );

        if (pluginMeta.getDescription() != null) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("description: "))
                    .append(Component.text(pluginMeta.getDescription(), CorkStyling.SECONDARY_COLOR))
            );
        }

        if (!pluginMeta.getAuthors().isEmpty()) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("authors: "))
                    .append(Component.text(String.join(", ", pluginMeta.getAuthors()), CorkStyling.SECONDARY_COLOR))
            );
        }

        if (!pluginMeta.getContributors().isEmpty()) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("contributors: "))
                    .append(Component.text(String.join(", ", pluginMeta.getContributors()), CorkStyling.SECONDARY_COLOR))
            );
        }

        if (pluginMeta.getWebsite() != null) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("website: "))
                    .append(Component.text(pluginMeta.getWebsite(), CorkStyling.SECONDARY_COLOR)
                            .clickEvent(ClickEvent.openUrl(pluginMeta.getWebsite())))
            );
        }

        if (!pluginMeta.getPluginDependencies().isEmpty()) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("dependencies: "))
                    .append(Component.text(String.join(", ", pluginMeta.getPluginDependencies()), CorkStyling.SECONDARY_COLOR))
            );
        }

        if (!pluginMeta.getPluginSoftDependencies().isEmpty()) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("soft-dependencies: "))
                    .append(Component.text(String.join(", ", pluginMeta.getPluginSoftDependencies()), CorkStyling.SECONDARY_COLOR))
            );
        }

        if (!pluginMeta.getLoadBeforePlugins().isEmpty()) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("load before: "))
                    .append(Component.text(String.join(", ", pluginMeta.getLoadBeforePlugins()), CorkStyling.SECONDARY_COLOR))
            );
        }

        infoLines.add(CorkStyling.INFO_LINE_PREFIX
                .append(Component.text("main class: "))
                .append(Component.text(pluginMeta.getMainClass(), CorkStyling.SECONDARY_COLOR))
        );


        sender.sendMessage(Component.join(
                JoinConfiguration.newlines(),
                infoLines
        ));
    }

    @Command("cork command <command>")
    @Permission("cork.command")
    public void corkCommandInfo(CommandSender sender, @Argument(value = "command", suggestions = "commands") String command) {
        RootCommandNode<CommandSourceStack> root = BukkitBrigForwardingMap.INSTANCE.getDispatcher().getRoot();
        CommandNode<CommandSourceStack> child = root.getChild(command.toLowerCase(Locale.ROOT));
        if (child == null) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("command doesn't exist")));
            return;
        }

        APICommandMeta apiCommandMeta = child.apiCommandMeta;
        if (apiCommandMeta == null || apiCommandMeta.pluginMeta() == null) {
            sender.sendMessage(CorkStyling.PREFIX.append(Component.text("no command metadata, is this a vanilla command?")));
            return;
        }

        PluginMeta pluginMeta = apiCommandMeta.pluginMeta();
        List<Component> infoLines = new ArrayList<>();
        infoLines.add(CorkStyling.PREFIX.append(Component.text("command"))
                .appendSpace()
                .append(Component.text(command, CorkStyling.PRIMARY_COLOR))
        );

        infoLines.add(CorkStyling.INFO_LINE_PREFIX
                .append(Component.text("registered by: "))
                .append(Component.text(pluginMeta.getName(), CorkStyling.SECONDARY_COLOR)
                        .clickEvent(ClickEvent.suggestCommand("/cork info %s".formatted(pluginMeta.getName()))))
        );

        if (!apiCommandMeta.aliases().isEmpty()) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("aliases: "))
                    .append(Component.text(String.join(", ", apiCommandMeta.aliases()), CorkStyling.SECONDARY_COLOR))
            );
        }

        if (apiCommandMeta.description() != null) {
            infoLines.add(CorkStyling.INFO_LINE_PREFIX
                    .append(Component.text("description: "))
                    .append(Component.text(apiCommandMeta.description(), CorkStyling.SECONDARY_COLOR))
            );
        }

        sender.sendMessage(Component.join(
                JoinConfiguration.newlines(),
                infoLines
        ));
    }
}
