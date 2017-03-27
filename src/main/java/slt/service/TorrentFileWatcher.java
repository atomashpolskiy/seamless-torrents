package slt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

class TorrentFileWatcher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentFileWatcher.class);

    private final List<File> sourceDirs;
    private final File queueDir;

    private final Object timer;
    private volatile boolean shutdown;

    TorrentFileWatcher(List<File> sourceDirs, File queueDir) {
        this.sourceDirs = sourceDirs;
        this.queueDir = queueDir;
        this.timer = new Object();
    }

    @Override
    public void run() {
        while (!shutdown) {
            sourceDirs.forEach(source -> {
                String[] torrents = source.list((dir, name) -> FileSystemTorrentSupplier.isTorrent(name));
                for (String torrent : torrents) {
                    File file = new File(source, torrent);
                    enqueue(file);
                }
            });

            try {
                synchronized (timer) {
                    timer.wait(1000);
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting, will shutdown...");
                shutdown();
            }
        }
    }

    private void enqueue(File file) {
        try {
            boolean renamed = file.renameTo(new File(queueDir, file.getName()));
            if (!renamed) {
                LOGGER.warn("Failed to move file '{}' to '{}'", file.getAbsolutePath(), queueDir.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error happened when trying to move file '%s' to '%s'",
                    file.getAbsolutePath(), queueDir.getAbsolutePath()), e);
        }
    }

    public void shutdown() {
        this.shutdown = true;
    }
}
