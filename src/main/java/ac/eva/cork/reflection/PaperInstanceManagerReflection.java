package ac.eva.cork.reflection;

import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@UtilityClass
public class PaperInstanceManagerReflection {
    public Map<String, Plugin> getPaperManagerLookupNames() {
        Object instanceManager = PaperPluginManagerReflection.getPaperInstanceManager();
        try {
            Field lookupNamesField = instanceManager.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);

            return (Map<String, Plugin>) lookupNamesField.get(instanceManager);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Plugin> getPaperManagerPlugins() {
        Object instanceManager = PaperPluginManagerReflection.getPaperInstanceManager();
        try {
            Field pluginsField = instanceManager.getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);

            return (List<Plugin>) pluginsField.get(instanceManager);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public MetaDependencyTree getPaperMetaDependencyTree() {
        Object instanceManager = PaperPluginManagerReflection.getPaperInstanceManager();
        try {
            Field dependencyTreeField = instanceManager.getClass().getDeclaredField("dependencyTree");
            dependencyTreeField.setAccessible(true);

            return (MetaDependencyTree) dependencyTreeField.get(instanceManager);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
