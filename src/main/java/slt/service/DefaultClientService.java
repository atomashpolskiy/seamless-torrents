package slt.service;

import bt.Bt;
import bt.BtClientBuilder;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class DefaultClientService implements ClientService {

    private static final Duration CLEANER_INTERVAL = Duration.ofMinutes(1);

    private final Supplier<BtRuntime> runtimeSupplier;
    private volatile Optional<BtRuntime> runtime;
    private final ReentrantLock lock;

    public DefaultClientService(Config config) {
        this.runtimeSupplier = () -> BtRuntime.builder(config).autoLoadModules().disableAutomaticShutdown().build();
        this.runtime = Optional.empty();
        this.lock = new ReentrantLock();

        initCleaner();
    }

    private void initCleaner() {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "slt-runtime-cleaner"));
        cleaner.scheduleAtFixedRate(new Cleaner(), CLEANER_INTERVAL.toMillis(), CLEANER_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(cleaner::shutdownNow));
    }

    @Override
    public BtClientBuilder buildClient() {
        return Bt.client(getRuntime());
    }

    private BtRuntime getRuntime() {
        lock.lock();
        try {
            return runtime.isPresent() ? runtime.get() : buildRuntime();
        } finally {
            lock.unlock();
        }
    }

    private BtRuntime buildRuntime() {
        BtRuntime newRuntime = runtimeSupplier.get();
        this.runtime = Optional.of(newRuntime);
        return newRuntime;
    }

    private class Cleaner implements Runnable {
        private final Duration DESTROY_RUNTIME_THRESHOLD = Duration.ofMinutes(5);

        private long inactiveSince;

        public void run() {
            lock.lock();
            try {
                if (runtime.isPresent() && runtime.get().isRunning()) {
                    Collection<BtClient> clients = runtime.get().getClients();
                    boolean active = false;
                    for (BtClient client : clients) {
                        if (client.isStarted()) {
                            active = true;
                            break;
                        }
                    }

                    if (!active) {
                        if (inactiveSince == 0) {
                            inactiveSince = System.currentTimeMillis();
                        }
                        if (System.currentTimeMillis() - inactiveSince >= DESTROY_RUNTIME_THRESHOLD.toMillis()) {
                            runtime.get().shutdown();
                            runtime = Optional.empty();
                        }
                    } else {
                        inactiveSince = 0;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
