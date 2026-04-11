package ac.eva.cork.reflection;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

@UtilityClass
public class BukkitJavaPluginReflection {
    public File getPluginFile(JavaPlugin plugin) {
        try {
            Field fileField = JavaPlugin.class.getDeclaredField("file");
            fileField.setAccessible(true);

            return (File) fileField.get(plugin);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
