package ac.eva.cork;

import ac.eva.cork.command.CorkInfoCommands;
import ac.eva.cork.command.CorkLifecycleCommands;
import ac.eva.cork.command.CorkSuggestions;
import ac.eva.cork.paper.CorkEntrypointHandler;
import ac.eva.cork.paper.storage.CorkServerPluginProviderStorage;
import ac.eva.cork.reflection.BukkitJavaPluginReflection;
import ac.eva.cork.reflection.BukkitSimplePluginManagerReflection;
import ac.eva.cork.reflection.PaperInstanceManagerReflection;
import ac.eva.cork.reflection.PaperSimpleProviderStorageReflection;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.command.brigadier.bukkit.BukkitBrigForwardingMap;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader;
import io.papermc.paper.plugin.entrypoint.strategy.PluginGraphCycleException;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.source.FileProviderSource;
import io.papermc.paper.plugin.storage.SimpleProviderStorage;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class CorkPlugin extends JavaPlugin {
    private static final FileProviderSource FILE_PROVIDER_SOURCE = new FileProviderSource("cork file '%s'"::formatted);
    private static final List<Entrypoint<?>> UNLOADED_ENTRYPOINTS = List.of(
            Entrypoint.BOOTSTRAPPER,
            Entrypoint.PLUGIN
    );

    @Override
    public void onEnable() {
        // todo: switch to non-legacy. this was just to enable hot reloading on the test server
        LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                this,
                ExecutionCoordinator.asyncCoordinator()
        );
        AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(commandManager, CommandSender.class);
        annotationParser.parse(
                new CorkSuggestions(this),
                new CorkInfoCommands(),
                new CorkLifecycleCommands(this)
        );
    }

    public Plugin loadPlugin(Path path) throws InvalidPluginException {
        for (Plugin otherPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (!(otherPlugin instanceof JavaPlugin otherJavaPlugin)) continue;

            Path jarPath = BukkitJavaPluginReflection.getPluginFile(otherJavaPlugin).toPath();
            if (jarPath.equals(path)) {
                throw new IllegalArgumentException("plugin is already loaded");
            }
        }

        CorkEntrypointHandler corkEntrypointHandler = new CorkEntrypointHandler(PaperInstanceManagerReflection.getPaperMetaDependencyTree());

        try {
            path = FILE_PROVIDER_SOURCE.prepareContext(path);
            FILE_PROVIDER_SOURCE.registerProviders(corkEntrypointHandler, path);
        } catch (IllegalArgumentException exception) {
            return null; // return null when the plugin file is not valid / plugin type is unknown
        } catch (PluginGraphCycleException exception) {
            throw new InvalidPluginException("cannot import plugin that causes cyclic dependencies!");
        } catch (Exception e) {
            throw new InvalidPluginException(e);
        }

        try {
            corkEntrypointHandler.enter(Entrypoint.BOOTSTRAPPER);
            corkEntrypointHandler.enter(Entrypoint.PLUGIN);
        } catch (Throwable e) {
            throw new InvalidPluginException(e);
        }

        CorkServerPluginProviderStorage serverPluginProviderStorage = (CorkServerPluginProviderStorage) corkEntrypointHandler.get(Entrypoint.PLUGIN);
        JavaPlugin lastPlugin = serverPluginProviderStorage.getLastPlugin();
        if (lastPlugin == null) {
            throw new InvalidPluginException("plugin didn't load any plugin providers?");
        }

        Bukkit.getPluginManager().enablePlugin(lastPlugin);

        // register commands
        PaperCommands.INSTANCE.setValid();
        LifecycleEventRunner.INSTANCE.callReloadableRegistrarEvent(LifecycleEvents.COMMANDS, PaperCommands.INSTANCE, Plugin.class, ReloadableRegistrarEvent.Cause.RELOAD);

        // sync commands to already existing players
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.syncCommands();

        return lastPlugin;
    }

    public void unloadPlugin(Plugin plugin) {
        // 1. disable
        Bukkit.getPluginManager().disablePlugin(plugin);

        // 2. update commands
        SimpleCommandMap commandMap = BukkitSimplePluginManagerReflection.getSimpleCommandMap();
        Map<String, Command> knownCommands = BukkitSimplePluginManagerReflection.getSimpleKnownCommands(commandMap);

        CommandDispatcher<CommandSourceStack> dispatcher = BukkitBrigForwardingMap.INSTANCE.getDispatcher();
        List<Map.Entry<String, Command>> pluginCommands = knownCommands.entrySet().stream()
                .filter(entry -> {
                    String name = entry.getKey();
                    if (name.contains(":")) {
                        return name.split(":")[0].equalsIgnoreCase(plugin.getName());
                    } else {
                        Command pluginCommand = knownCommands.get(plugin.getName().toLowerCase(Locale.ENGLISH) + ":" + name.toLowerCase(Locale.ENGLISH));
                        if (pluginCommand != null && pluginCommand == entry.getValue()) {
                            return true;
                        }

                        CommandNode<CommandSourceStack> child = dispatcher.getRoot().getChild(name);
                        if (child == null) return false;

                        if (child.apiCommandMeta != null) {
                            PluginMeta pluginMeta = child.apiCommandMeta.pluginMeta();
                            if (pluginMeta == null) return false;

                            return pluginMeta.getName().equalsIgnoreCase(plugin.getName());
                        }

                        return false;
                    }
                })
                .toList();

        for (Map.Entry<String, Command> commandEntry : pluginCommands) {
            Command command = commandEntry.getValue();
            command.unregister(commandMap);
            knownCommands.remove(command.getName());
        }

        CraftServer server = (CraftServer) Bukkit.getServer();
        server.syncCommands();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.updateCommands();
        }

        // 3. remove from SimplePluginManager plugin lists
        List<Plugin> simpleManagerPlugins = BukkitSimplePluginManagerReflection.getSimpleManagerPlugins();
        Map<String, Plugin> simpleManagerLookupNames = BukkitSimplePluginManagerReflection.getSimpleManagerLookupNames();

        simpleManagerPlugins.removeIf(op -> op.getName().equalsIgnoreCase(plugin.getName()));
        simpleManagerLookupNames.remove(plugin.getName().toLowerCase(Locale.ENGLISH));

        // 4. remove from PaperPluginInstanceManager plugin lists
        Map<String, Plugin> paperManagerLookupNames = PaperInstanceManagerReflection.getPaperManagerLookupNames();
        List<Plugin> paperManagerPlugins = PaperInstanceManagerReflection.getPaperManagerPlugins();

        paperManagerPlugins.removeIf(op -> op.getName().equalsIgnoreCase(plugin.getName()));
        paperManagerLookupNames.remove(plugin.getName().toLowerCase(Locale.ENGLISH));

        // 5. remove from LaunchEntrypointHandler providers
        for (Entrypoint<?> entrypoint : UNLOADED_ENTRYPOINTS) {
            SimpleProviderStorage<?> storage = (SimpleProviderStorage<?>) LaunchEntryPointHandler.INSTANCE.get(entrypoint);
            List<PluginProvider<?>> providers = PaperSimpleProviderStorageReflection.getStorageProviders(storage);

            for (PluginProvider<?> provider : new ArrayList<>(providers)) {
                if (!provider.getMeta().getName().equalsIgnoreCase(plugin.getName())) {
                    continue;
                }

                providers.remove(provider);
            }
        }

        // 6. close classloader
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        if (classLoader instanceof PaperPluginClassLoader paperPluginClassLoader) {
            try {
                paperPluginClassLoader.close();
            } catch (IOException ex) {
                log.error("failed to close paper plugin class loader", ex);
            }

            return;
        }

        if (classLoader instanceof PluginClassLoader pluginClassLoader) {
            try {
                Field pluginField = PluginClassLoader.class.getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(pluginClassLoader, null);

                Field pluginInitField = PluginClassLoader.class.getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(pluginClassLoader, null);

                pluginClassLoader.close();
            } catch (IOException | NoSuchFieldException | IllegalAccessException  ex) {
                log.error("failed to close plugin class loader", ex);
            }

            return;
        }

        log.warn("plugin class loader was unknown, skipped close (classloader={})", classLoader.getClass());
    }

    public Path getPluginsDirectory() {
        return getDataPath().getParent();
    }
}
