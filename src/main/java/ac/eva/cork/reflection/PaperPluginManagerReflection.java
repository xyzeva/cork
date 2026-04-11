package ac.eva.cork.reflection;

import io.papermc.paper.plugin.manager.PaperPluginManagerImpl;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;

@UtilityClass
public class PaperPluginManagerReflection {
    public PaperPluginManagerImpl getPaperPluginManager() {
        SimplePluginManager simplePluginManager = (SimplePluginManager) Bukkit.getPluginManager();

        return (PaperPluginManagerImpl) simplePluginManager.paperPluginManager;
    }

    public Object getPaperInstanceManager() {
        PaperPluginManagerImpl paperPluginManager = getPaperPluginManager();

        try {
            Field instanceManagerField = PaperPluginManagerImpl.class.getDeclaredField("instanceManager");
            instanceManagerField.setAccessible(true);

            return instanceManagerField.get(paperPluginManager);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
