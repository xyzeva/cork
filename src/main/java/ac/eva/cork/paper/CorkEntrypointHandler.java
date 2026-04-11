package ac.eva.cork.paper;

import ac.eva.cork.paper.storage.CorkBootstrapProviderStorage;
import ac.eva.cork.paper.storage.CorkServerPluginProviderStorage;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.EntrypointHandler;
import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.storage.ProviderStorage;

import java.util.HashMap;
import java.util.Map;

public class CorkEntrypointHandler implements EntrypointHandler {
    private final Map<Entrypoint<?>, ProviderStorage<?>> storage = new HashMap<>();

    public CorkEntrypointHandler(MetaDependencyTree dependencyTree) {
        this.storage.put(Entrypoint.BOOTSTRAPPER, new CorkBootstrapProviderStorage(dependencyTree));
        this.storage.put(Entrypoint.PLUGIN, new CorkServerPluginProviderStorage(dependencyTree));
    }

    @Override
    public <T> void register(Entrypoint<T> entrypoint, PluginProvider<T> provider) {
        ProviderStorage<T> storage = this.get(entrypoint);
        if (storage == null) {
            throw new IllegalArgumentException("no storage registered for entrypoint %s".formatted(entrypoint));
        }

        storage.register(provider);    }

    @Override
    public void enter(Entrypoint<?> entrypoint) {
        ProviderStorage<?> storage = this.storage.get(entrypoint);
        if (storage == null) {
            throw new IllegalArgumentException("no storage registered for entrypoint %s".formatted(entrypoint));
        }

        storage.enter();
    }

    public <T> ProviderStorage<T> get(Entrypoint<T> entrypoint) {
        return (ProviderStorage<T>) this.storage.get(entrypoint);
    }

}