# cork

a plugin manager for paper servers that supports hot reloading paper plugins

## installation

you have to have a paper-based server (versions older than 1.21 untested and unsupported)

you can download the plugin jar file from [the latest github release](https://github.com/xyzeva/cork/releases/latest) and put it in your plugins folder

## limits

i would obviously not load huge plugins (especially with huge bootstrapper stages) in with cork in an environment that isn't testing as it can cause issues.

but, cork is pretty good at loading small plugins and ones without huge bootstrapper stages in. making it a good use-case for small plugins that you need to hotfix something with.

## how

### loading

cork has its own plugin entrypoint handler called `CorkEntrypointHandler` that it loads plugins with. `CorkEntrypointHandler` behaves basically the same as paper's `LaunchEntrypointHandler` except for a few changes:
- cork as its own `ConfiguredProviderStorage` classes extending `BootstrapProviderStorage` and `ServerPluginProviderStorage` for bootstraps and server plugins respectively. these work on a set dependency tree grabbed via reflection from paper's plugin manager instead of creating a new one
- these provider storages, while used at runtime, bypass the constraint of not loading paper plugins by skipping a simple check

cork then simply enables the loaded plugin and recalls the `COMMANDS` reloadable lifecycle event for plugins to re-register their commands after a load.

### unloading

unloading is a bit more complicated, but this is the things that happen in order:
- normally disable the plugin using `SimplePluginManager#disablePlugin`
- unregister all the plugins commands and sync the command list to players
- remove the plugin from `SimplePluginManager` plugin lists
- remove the plugin from `PaperPluginInstanceManager` plugin lists
- remove the plugin from `LaunchEntrypointHandler` providers
- close the plugin's classloader