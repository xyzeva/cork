package ac.eva.cork.paper.storage;

import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.storage.BootstrapProviderStorage;
import io.papermc.paper.plugin.storage.ServerPluginProviderStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class CorkServerPluginProviderStorage extends ServerPluginProviderStorage {
    private final MetaDependencyTree dependencyTree;
    @Getter
    private JavaPlugin lastPlugin;

    @Override
    public void register(PluginProvider<JavaPlugin> provider) {
        super.register(provider);
        LaunchEntryPointHandler.INSTANCE.register(Entrypoint.PLUGIN, provider);
    }

    @Override
    public void processProvided(PluginProvider<JavaPlugin> provider, JavaPlugin provided) {
        super.processProvided(provider, provided);
        this.lastPlugin = provided;
    }

    @Override
    public MetaDependencyTree createDependencyTree() {
        return this.dependencyTree;
    }
}
