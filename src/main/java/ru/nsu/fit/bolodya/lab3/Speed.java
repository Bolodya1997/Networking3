package ru.nsu.fit.bolodya.lab3;

import java.util.*;

public class Speed {

    private static final int INTERVAL_IN_SEC = 5;
    private static final int INTERVAL_IN_MILLIS = INTERVAL_IN_SEC * 1000;

    private Map<Long, Long> last = new HashMap<>();

    synchronized public void put(int amount) {
        long cur = System.currentTimeMillis();
        if (last.containsKey(cur))
            amount += last.get(cur);
        last.put(cur, (long) amount);
    }

    private void clear() {
        long cur = System.currentTimeMillis();
        Iterator<Map.Entry<Long, Long>> iterator = last.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            if (cur - entry.getKey() > INTERVAL_IN_MILLIS)
                iterator.remove();
        }
    }

    public synchronized String getSpeed() {
        clear();

        long sum = 0;
        for (Map.Entry<Long, Long> entry : last.entrySet())
            sum += entry.getValue();

        return speedDecode(((double) sum) / INTERVAL_IN_SEC);
    }

    private String speedDecode(double speed) {
        if (speed < 1024)
            return String.format("%.3f b/s", speed);

        speed /= 1024;
        if (speed < 1024)
            return String.format("%.3f Kb/s", speed);

        speed /= 1024;
        if (speed < 1024)
            return String.format("%.3f Mb/s", speed);

        speed /= 1024;
        return String.format("%.3f Gb/s", speed);
    }
}
