package ac.eva.cork.paper.storage;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.storage.BootstrapProviderStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorkBootstrapProviderStorage extends BootstrapProviderStorage {
    private final MetaDependencyTree dependencyTree;

    @Override
    public void register(PluginProvider<PluginBootstrap> provider) {
        super.register(provider);
        LaunchEntryPointHandler.INSTANCE.register(Entrypoint.BOOTSTRAPPER, provider);
    }

    @Override
    public MetaDependencyTree createDependencyTree() {
        return this.dependencyTree;
    }
}
