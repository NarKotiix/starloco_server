package org.starloco.locos.fight.ia;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.kernel.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lightweight AI profiler focused on invocation turns and hotspot methods.
 * Disabled by default via config flags.
 */
public final class IAProfiler {

    private static final Logger AIPROF_LOGGER = LoggerFactory.getLogger("ai.profiling");

    private static final ConcurrentMap<Long, Long> TURN_START_NS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Stat> STATS = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_DUMP_NS = new AtomicLong(0L);

    private static final long SUMMARY_INTERVAL_NS = TimeUnit.SECONDS.toNanos(30);

    private IAProfiler() {
    }

    public static void startTurn(Fight fight, Fighter fighter) {
        if (!isEnabledFor(fighter) || fight == null) {
            return;
        }
        TURN_START_NS.put(turnKey(fight, fighter), System.nanoTime());
    }

    public static void endTurn(Fight fight, Fighter fighter, String reason) {
        if (!isEnabledFor(fighter) || fight == null) {
            return;
        }
        Long startNs = TURN_START_NS.remove(turnKey(fight, fighter));
        if (startNs == null) {
            return;
        }
        long elapsedNs = System.nanoTime() - startNs;
        record("turn.total", elapsedNs);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
        if (elapsedMs >= Config.getInstance().AIProfilingWarnMs) {
            AIPROF_LOGGER.info("[AI-PROF] slow turn={}ms fight={} fighter={} mob={} invoc={} reason={}",
                    elapsedMs,
                    fight.getId(),
                    fighter.getId(),
                    fighterMobLabel(fighter),
                    fighter.isInvocation(),
                    reason == null ? "" : reason);
        }

        maybeDumpSummary();
    }

    public static long methodStart(Fighter fighter, String metric) {
        if (!isEnabledFor(fighter) || metric == null || metric.isEmpty()) {
            return 0L;
        }
        return System.nanoTime();
    }

    public static void methodEnd(Fighter fighter, String metric, long startNs, String context) {
        if (startNs == 0L || !isEnabledFor(fighter) || metric == null || metric.isEmpty()) {
            return;
        }
        long elapsedNs = System.nanoTime() - startNs;
        record(metric, elapsedNs);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
        if (elapsedMs >= Config.getInstance().AIProfilingWarnMs) {
            int fightId = fighter.getFight() != null ? fighter.getFight().getId() : -1;
            AIPROF_LOGGER.info("[AI-PROF] slow method={} {}ms fight={} fighter={} mob={} ctx={}",
                    metric,
                    elapsedMs,
                    fightId,
                    fighter.getId(),
                    fighterMobLabel(fighter),
                    context == null ? "" : context);
        }

        maybeDumpSummary();
    }

    private static boolean isEnabledFor(Fighter fighter) {
        Config config = Config.getInstance();
        if (!config.AIProfiling) {
            return false;
        }
        return !config.AIProfilingInvocationOnly || (fighter != null && fighter.isInvocation());
    }

    private static long turnKey(Fight fight, Fighter fighter) {
        long f = (long) fight.getId() & 0xFFFFFFFFL;
        long i = (long) fighter.getId() & 0xFFFFFFFFL;
        return (f << 32) | i;
    }

    private static void record(String metric, long elapsedNs) {
        Stat stat = STATS.computeIfAbsent(metric, k -> new Stat());
        stat.calls.increment();
        stat.totalNs.add(elapsedNs);
        stat.updateMax(elapsedNs);
    }

    private static void maybeDumpSummary() {
        long now = System.nanoTime();
        long last = LAST_DUMP_NS.get();
        if (now - last < SUMMARY_INTERVAL_NS) {
            return;
        }
        if (!LAST_DUMP_NS.compareAndSet(last, now)) {
            return;
        }

        List<Map.Entry<String, Stat>> entries = new ArrayList<>(STATS.entrySet());
        entries.sort(Comparator.comparingLong((Map.Entry<String, Stat> e) -> e.getValue().totalNs.sum()).reversed());

        int limit = Math.min(5, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Stat> entry = entries.get(i);
            long calls = entry.getValue().calls.sum();
            long totalNs = entry.getValue().totalNs.sum();
            long maxNs = entry.getValue().maxNs.get();
            if (calls <= 0) {
                continue;
            }
            long avgMicros = TimeUnit.NANOSECONDS.toMicros(totalNs / calls);
            long maxMicros = TimeUnit.NANOSECONDS.toMicros(maxNs);
            AIPROF_LOGGER.info("[AI-PROF] summary metric={} calls={} avg={}us max={}us", entry.getKey(), calls, avgMicros, maxMicros);
        }
    }

    private static String fighterMobLabel(Fighter fighter) {
        if (fighter == null) {
            return "n/a";
        }
        if (fighter.getMob() != null && fighter.getMob().getTemplate() != null) {
            int mobId = fighter.getMob().getTemplate().getId();
            int mobIa = fighter.getMob().getTemplate().getIa();
            return mobId + " - Mob#" + mobId + " (IA " + mobIa + ")";
        }
        if (fighter.getPersonnage() != null) {
            return fighter.getPersonnage().getName();
        }
        return "type=" + fighter.getType();
    }

    private static final class Stat {
        private final LongAdder calls = new LongAdder();
        private final LongAdder totalNs = new LongAdder();
        private final AtomicLong maxNs = new AtomicLong();

        private void updateMax(long value) {
            long prev = maxNs.get();
            while (value > prev && !maxNs.compareAndSet(prev, value)) {
                prev = maxNs.get();
            }
        }
    }
}

