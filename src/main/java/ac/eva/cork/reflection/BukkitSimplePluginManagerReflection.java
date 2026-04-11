package ac.eva.cork.reflection;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@UtilityClass
public class BukkitSimplePluginManagerReflection {
    public SimpleCommandMap getSimpleCommandMap() {
        SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            return (SimpleCommandMap) commandMapField.get(pluginManager);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Command> getSimpleKnownCommands(SimpleCommandMap commandMap) {
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            return (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Plugin> getSimpleManagerPlugins() {
        SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
        try {
            Field pluginsField = SimplePluginManager.class.getDeclaredField("plugins");
            pluginsField.setAccessible(true);

            return (List<Plugin>) pluginsField.get(pluginManager);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Plugin> getSimpleManagerLookupNames() {
        SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
        try {
            Field lookupNamesField = pluginManager.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);

            return (Map<String, Plugin>) lookupNamesField.get(pluginManager);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
