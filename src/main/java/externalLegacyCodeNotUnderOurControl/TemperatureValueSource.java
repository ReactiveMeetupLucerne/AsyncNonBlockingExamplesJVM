package externalLegacyCodeNotUnderOurControl;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TemperatureValueSource {

    private final List<TemperatureListener> listenerList = Lists.newCopyOnWriteArrayList();

    private final ScheduledExecutorService executorService;

    public TemperatureValueSource() {
        executorService = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("source-%d").build()
        );
        executorService.scheduleAtFixedRate(() -> {
            int nextTemperature = ThreadLocalRandom.current().nextInt(20, 25);
            listenerList.forEach(listener -> listener.onNext(nextTemperature));
        }, 2, 2, TimeUnit.SECONDS);
    }

    public void addListener(TemperatureListener listener) {
        listenerList.add(listener);
    }

    public interface TemperatureListener {

        void onNext(int temperature);
    }
}
