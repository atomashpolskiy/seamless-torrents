package slt;

import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.runtime.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.annotation.BQConfigProperty;
import slt.service.ClientService;
import slt.service.DefaultClientService;
import slt.service.FileSystemTorrentSupplier;
import slt.service.FilesystemService;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ConfigurableServiceFactory {

    private Config config;
    private String outputDir;
    private List<String> sourceDirs;
    private String queueDir;

    public ConfigurableServiceFactory() {
        this.sourceDirs = Collections.emptyList();
    }

    @BQConfigProperty("Bt Core runtime configuration")
    @JsonProperty("runtime")
    public void setConfig(Config config) {
        this.config = config;
    }

    @BQConfigProperty("Directory to download torrents to")
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    @BQConfigProperty("List of directories to watch for new .torrent files")
    public void setSourceDirs(List<String> sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    @BQConfigProperty("Directory that acts as a processing queue")
    public void setQueueDir(String queueDir) {
        this.queueDir = queueDir;
    }

    public ClientService buildRuntimeService() {
        Config config = new Config(this.config);
        config.setNumOfHashingThreads(Runtime.getRuntime().availableProcessors());
        return new DefaultClientService(config);
    }

    public Storage buildStorage(FilesystemService filesystemService) {
        File f = filesystemService.getOrCreateDirectory(outputDir);
        return new FileSystemStorage(f);
    }

    public Supplier<DownloadItem> buildTorrentSupplier(FilesystemService filesystemService) {
        if (sourceDirs.isEmpty()) {
            throw new IllegalStateException("Source directories are not specified");
        }
        Objects.requireNonNull(queueDir, "Queue directory is not specified");
        return new FileSystemTorrentSupplier(filesystemService, sourceDirs, queueDir);
    }
}
