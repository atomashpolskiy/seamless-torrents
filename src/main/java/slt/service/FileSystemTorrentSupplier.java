package slt.service;

import slt.DownloadItem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FileSystemTorrentSupplier implements Supplier<DownloadItem> {
    private FilesystemService filesystemService;
    // these directories are watched for new .torrent files
    private List<File> sourceDirs;
    // new .torrent files are moved in this directory for processing
    private File queueDir;

    public FileSystemTorrentSupplier(FilesystemService filesystemService,
                                     List<String> sourceDirs,
                                     String queueDir) {
        this.filesystemService = filesystemService;
        this.sourceDirs = mapDirs(sourceDirs);
        this.queueDir = filesystemService.getOrCreateDirectory(queueDir);

        initWatcher(this.sourceDirs, this.queueDir);
    }

    private void initWatcher(List<File> sourceDirs, File queueDir) {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "slt-file-watcher"));
        TorrentFileWatcher watcher = new TorrentFileWatcher(sourceDirs, queueDir);
        Runtime.getRuntime().addShutdownHook(new Thread(watcher::shutdown));
        executor.execute(watcher);
    }

    private List<File> mapDirs(List<String> sourceDirs) {
        return sourceDirs.stream().map(filesystemService::getOrCreateDirectory).collect(Collectors.toList());
    }

    @Override
    public DownloadItem get() {
        File[] files = queueDir.listFiles((dir, name) -> isTorrent(name));
        if (files == null) {
            throw new IllegalStateException("Unexpected state: queue file is not a directory");
        } else if (files.length == 0) {
            return null;
        }

        List<File> list = new ArrayList<>(Arrays.asList(files));
        list.sort(LastModifiedComparator.oldFirst());

        File file = list.get(0);
        return new DownloadItem() {
            @Override
            public URL torrentUrl() {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Unexpected error", e);
                }
            }

            @Override
            public void completeAndCleanup() {
                file.delete();
            }
        };
    }

    static boolean isTorrent(String fileName) {
        Optional<String> extension = getExtension(fileName);
        return extension.isPresent() && extension.get().equals(".torrent");
    }

    private static Optional<String> getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(fileName.substring(dotIndex));
    }
}
