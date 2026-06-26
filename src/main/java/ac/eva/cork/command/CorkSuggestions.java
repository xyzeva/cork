package ac.eva.cork.command;

import ac.eva.cork.CorkPlugin;
import io.papermc.paper.command.brigadier.bukkit.BukkitBrigForwardingMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandInput;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
public class CorkSuggestions {
    private final CorkPlugin plugin;

    @Suggestions("pluginFiles")
    public List<String> pluginFileSuggestions(CommandInput input) {
        String inputPath = input.remainingInput().stripLeading();
        Path pluginsDir = this.plugin.getPluginsDirectory().toAbsolutePath().normalize();
        PluginFileSearch pluginFileSearch = getPluginFileSearch(inputPath, pluginsDir);

        Path searchDir = pluginFileSearch.searchDir().toAbsolutePath().normalize();
        if (!searchDir.startsWith(pluginsDir)) {
            return List.of();
        }

        File searchDirFile = searchDir.toFile();
        if (!searchDirFile.exists() || !searchDirFile.isDirectory()) {
            return List.of();
        }

        File[] files = searchDirFile.listFiles((d, name) -> name.toLowerCase(Locale.ENGLISH).endsWith(".jar"));
        if (files == null) return List.of();

        String filePrefix = pluginFileSearch.filePrefix().toLowerCase(Locale.ENGLISH);
        CorkPlugin.LoadedPluginSnapshot loadedPluginSnapshot = this.plugin.getLoadedPluginSnapshot();

        return Arrays.stream(files)
                .map(file -> PluginFileSuggestion.from(file, this.plugin, loadedPluginSnapshot))
                .filter(suggestion -> !suggestion.isLoaded(loadedPluginSnapshot))
                .filter(suggestion -> suggestion.matches(filePrefix))
                .map(suggestion -> pluginFileSearch.directoryInput() + suggestion.fileName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Suggestions("plugins")
    public List<String> pluginsSuggestions(String input) {
        String pluginPrefix = input.toLowerCase(Locale.ENGLISH);
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .filter(pluginName -> pluginPrefix.isEmpty() || pluginName.toLowerCase(Locale.ENGLISH).startsWith(pluginPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Suggestions("commands")
    public List<String> commandSuggestions() {
        return new ArrayList<>(BukkitBrigForwardingMap.INSTANCE.keySet());
    }

    private PluginFileSearch getPluginFileSearch(String inputPath, Path pluginsDir) {
        int lastSeparator = Math.max(inputPath.lastIndexOf('/'), inputPath.lastIndexOf('\\'));
        String directoryInput = "";
        String filePrefix = inputPath;

        if (lastSeparator >= 0) {
            directoryInput = inputPath.substring(0, lastSeparator + 1);
            filePrefix = inputPath.substring(lastSeparator + 1);
        }

        String resolvedDirectory = directoryInput
                .replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);

        return new PluginFileSearch(
                directoryInput,
                filePrefix,
                pluginsDir.resolve(resolvedDirectory).normalize()
        );
    }

    private record PluginFileSearch(String directoryInput, String filePrefix, Path searchDir) {
    }

    private record PluginFileSuggestion(String fileName, Path normalizedPath, Optional<String> pluginName) {
        private static PluginFileSuggestion from(File file, CorkPlugin plugin, CorkPlugin.LoadedPluginSnapshot loadedPluginSnapshot) {
            Path filePath = file.toPath();
            Path normalizedPath = plugin.normalizePluginPath(filePath);
            if (loadedPluginSnapshot.paths().contains(normalizedPath)) {
                return new PluginFileSuggestion(file.getName(), normalizedPath, Optional.empty());
            }

            return new PluginFileSuggestion(
                    file.getName(),
                    normalizedPath,
                    plugin.getPluginName(filePath)
            );
        }

        private boolean isLoaded(CorkPlugin.LoadedPluginSnapshot loadedPluginSnapshot) {
            return loadedPluginSnapshot.contains(this.normalizedPath, this.pluginName);
        }

        private boolean matches(String prefix) {
            if (prefix.isEmpty()) {
                return true;
            }

            if (this.fileName.toLowerCase(Locale.ENGLISH).startsWith(prefix)) {
                return true;
            }

            return this.pluginName
                    .map(name -> name.toLowerCase(Locale.ENGLISH).startsWith(prefix))
                    .orElse(false);
        }
    }
}
