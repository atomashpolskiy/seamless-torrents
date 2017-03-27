package slt;

import bt.data.Storage;
import bt.metainfo.Torrent;
import bt.runtime.BtClient;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import slt.jna.NSUserNotification;
import slt.jna.NSUserNotificationCenter;
import slt.jna.NSUserNotificationCenterDelegate;
import slt.service.ClientService;
import slt.service.TorrentSupplier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class DaemonCommand extends CommandWithMetadata {

    @Inject
    private Provider<ClientService> clientService;

    @Inject @TorrentSupplier
    private Provider<Supplier<DownloadItem>> torrentSupplier;

    @Inject
    private Provider<Storage> storage;

    private volatile NSUserNotificationCenter notificationCenter;
    private final Object lock;

    private volatile boolean shutdown;

    public DaemonCommand() {
        super(CommandMetadata.builder(DaemonCommand.class));
        this.lock = new Object();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public CommandOutcome run(Cli cli) {
        while (!shutdown) {
            try {
                DownloadItem item = torrentSupplier.get().get();
                if (item == null) {
                    Thread.sleep(1000);
                    continue;
                }
                process(item);
            } catch (InterruptedException e) {
                return CommandOutcome.failed(-1, "Main loop was interrupted");
            } catch (Throwable e) {
                return CommandOutcome.failed(-1, e);
            }
        }
        return CommandOutcome.succeeded();
    }

    private void process(DownloadItem item) {
        // TODO: custom selector that performs sequential/rarest selection based on current config option value (updated by user)
        BtClient client = clientService.get().buildClient().storage(storage.get()).torrent(item.torrentUrl()).build();

        Torrent torrent = client.getSession().getTorrent();
        notifyDownloadStarted(torrent);

        AtomicBoolean completed = new AtomicBoolean(false);
        client.startAsync(state -> {
            // TODO: update UI with current DL rate/completed ratio/time remaining
            if (state.getPiecesRemaining() == 0) {
                completed.set(true);
                try {
                    client.stop();
                } catch (Exception e) {
                    // ignore
                }
            }
        }, 1000).join();

        if (completed.get()) {
            item.completeAndCleanup();
            notifyDownloadCompleted(torrent);
        } else {
            notifyDownloadStopped(torrent);
        }
    }

    private void notifyDownloadStarted(Torrent torrent) {
        NSUserNotification notification = NSUserNotification.create();
        notification.setTitle("Download started");
        notification.setInformative​Text("Now downloading: " + torrent.getName());

        deliver(notification);
    }

    private void notifyDownloadStopped(Torrent torrent) {
        NSUserNotification notification = NSUserNotification.create();
        notification.setTitle("Download stopped");
        notification.setInformative​Text("Stopped: " + torrent.getName());

        deliver(notification);
    }

    private void notifyDownloadCompleted(Torrent torrent) {
        NSUserNotification notification = NSUserNotification.create();
        notification.setTitle("Download completed");
        notification.setInformative​Text("Completed: " + torrent.getName());

        deliver(notification);
    }

    private synchronized void deliver(NSUserNotification notification) {
        getNotificationCenter().deliverNotification(notification);
    }

    private NSUserNotificationCenter getNotificationCenter() {
        if (notificationCenter == null) {
            synchronized (lock) {
                if (notificationCenter == null) {
                    NSUserNotificationCenterDelegate delegate = (center, notification1) -> {
                        // always show notifications
                        return true;
                    };

                    NSUserNotificationCenter notificationCenter = NSUserNotificationCenter.defaultCenter();
                    notificationCenter.setDelegate(delegate);
                    this.notificationCenter = notificationCenter;
                }
            }
        }
        return notificationCenter;
    }

    public void shutdown() {
        this.shutdown = true;
    }
}
