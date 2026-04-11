package ac.eva.cork.reflection;

import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.storage.SimpleProviderStorage;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.List;

@UtilityClass
public class PaperSimpleProviderStorageReflection {
    public List<PluginProvider<?>> getStorageProviders(SimpleProviderStorage<?> storage) {
        try {
            Field providersField = SimpleProviderStorage.class.getDeclaredField("providers");
            providersField.setAccessible(true);

            return (List<PluginProvider<?>>) providersField.get(storage);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
