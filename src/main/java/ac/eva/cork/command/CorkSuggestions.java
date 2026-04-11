package ac.eva.cork.command;

import ac.eva.cork.CorkPlugin;
import io.papermc.paper.command.brigadier.bukkit.BukkitBrigForwardingMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CorkSuggestions {
    private final CorkPlugin plugin;

    @Suggestions("pluginFiles")
    public List<String> pluginFileSuggestions(CommandContext<CommandSender> ctx, CommandInput input) {
        String inputPath = input.readString();
        Path pluginsDir = this.plugin.getPluginsDirectory();
        Path searchDir = pluginsDir;
        if (!inputPath.isEmpty()) {
            searchDir = pluginsDir.resolve(inputPath);
        }

        File searchDirFile = searchDir.toFile();
        if (!searchDirFile.exists() || !searchDirFile.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = searchDirFile.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null) return List.of();

        String inputPathWithDelimiter = inputPath.endsWith(File.separator) ? inputPath : inputPath + File.separator;
        return Arrays.stream(files)
                .map(f -> inputPathWithDelimiter + f.getName())
                .toList();
    }

    @Suggestions("plugins")
    public List<String> pluginsSuggestions() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .toList();
    }

    @Suggestions("commands")
    public List<String> commandSuggestions() {
        return new ArrayList<>(BukkitBrigForwardingMap.INSTANCE.keySet());
    }
}
