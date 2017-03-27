package slt;

import bt.data.Storage;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jna.NativeLibrary;
import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.config.ConfigurationFactory;
import slt.service.ClientService;
import slt.service.DefaultFilesystemService;
import slt.service.FilesystemService;
import slt.service.TorrentSupplier;

import java.util.Objects;
import java.util.function.Supplier;

public class Application implements Module {
    public static final String USER_DIR_ENVVAR = "USER_DIR";
    public static final String ROCOCOA_HOME_ENV = "ROCOCOA_HOME";

    private static final String CONFIG_PREFIX = "config";

    public static void main(String[] args) {
        NativeLibrary.addSearchPath("rococoa",
                Objects.requireNonNull(System.getenv(ROCOCOA_HOME_ENV), "Rococoa library home is not specified"));

        Bootique.app(args).autoLoadModules().module(Application.class).run();
    }

    @Override
    public void configure(Binder binder) {
        BQCoreModule.contributeCommands(binder).addBinding().to(InitCommand.class);
        BQCoreModule.contributeCommands(binder).addBinding().to(DaemonCommand.class);

        binder.bind(FilesystemService.class).to(DefaultFilesystemService.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public ClientService provideRuntimeService(ConfigurationFactory configurationFactory) {
        return configurationFactory.config(ConfigurableServiceFactory.class, CONFIG_PREFIX).buildRuntimeService();
    }

    @Provides
    @Singleton
    @TorrentSupplier
    public Supplier<DownloadItem> provideTorrentSupplier(ConfigurationFactory configurationFactory, FilesystemService filesystemService) {
        return configurationFactory.config(ConfigurableServiceFactory.class, CONFIG_PREFIX).buildTorrentSupplier(filesystemService);
    }

    @Provides
    @Singleton
    public Storage provideStorage(ConfigurationFactory configurationFactory, FilesystemService filesystemService) {
        return configurationFactory.config(ConfigurableServiceFactory.class, CONFIG_PREFIX).buildStorage(filesystemService);
    }
}
