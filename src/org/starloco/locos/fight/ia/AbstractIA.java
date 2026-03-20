package org.starloco.locos.fight.ia;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.game.world.World;
import org.starloco.locos.kernel.Constant;
import org.starloco.locos.kernel.Config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Locos on 18/09/2015.
 * Optimized: shared pool (no thread-per-instance), non-blocking addNext, AtomicBoolean terminated flag.
 */
public abstract class AbstractIA implements IA {

    /**
     * Pool partagé par toutes les instances d'IA.
     * Évite la création d'un thread par monstre par combat (N*M threads).
     * Taille = max(2, nbCPU) pour absorber les combats parallèles.
     */
    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "IA-Pool");
                t.setDaemon(true);
                return t;
            }
    );

    protected Fight fight;
    protected Fighter fighter;
    protected boolean stop;
    protected byte count;

    /** Empêche l'exécution de tâches planifiées après la fin du tour de cette IA. */
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private long blockedSinceMs = 0L;
    private int blockedCycles = 0;
    private static final long STUCK_BLOCK_TIMEOUT_MS = 2500L;
    private long endTurnPollStartMs = 0L;
    private int endTurnPollCycles = 0;
    private static final long STUCK_ENDTURN_TIMEOUT_MS = 2500L;

    public AbstractIA(Fight fight, Fighter fighter, byte count) {
        this.fight = fight;
        this.fighter = fighter;
        this.count = count;
    }

    @Override public Fight getFight()               { return fight;   }
    @Override public Fighter getFighter()           { return fighter; }
    @Override public boolean isStop()               { return stop;    }
    @Override public void setStop(boolean stop)     { this.stop = stop; }

    @Override
    public void endTurn() {
        if (terminated.get()) return;

        if (this.stop && !this.fighter.isDead()) {
            endTurnPollStartMs = 0L;
            endTurnPollCycles = 0;
            terminated.set(true);
            if (this.fighter.haveInvocation()) {
                // Planifier via le pool — ne pas bloquer le thread courant
                POOL.schedule(() -> {
                    IAProfiler.endTurn(this.fight, this.fighter, "invocation");
                    this.fight.endTurn(false, this.fighter);
                }, 0, TimeUnit.MILLISECONDS);
            } else {
                IAProfiler.endTurn(this.fight, this.fighter, "normal");
                this.fight.endTurn(false, this.fighter);
            }
        } else {
            if (!this.fight.isFinish()) {
                long now = System.currentTimeMillis();
                if (endTurnPollStartMs == 0L)
                    endTurnPollStartMs = now;
                endTurnPollCycles++;

                long pollingForMs = now - endTurnPollStartMs;
                if (Config.getInstance().AIProfiling && (endTurnPollCycles == 1 || endTurnPollCycles % 20 == 0)) {
                    World.world.logger.info("[AI-PROF] endTurn polling fight={} fighter={} cycles={} elapsed={}ms stop={}",
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1,
                            endTurnPollCycles,
                            pollingForMs,
                            this.stop);
                }

                if (pollingForMs >= STUCK_ENDTURN_TIMEOUT_MS) {
                    World.world.logger.warn("[AI-PROF] endTurn stuck, force pass turn fight={} fighter={} cycles={} elapsed={}ms",
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1,
                            endTurnPollCycles,
                            pollingForMs);
                    this.stop = true;
                    endTurn();
                    return;
                }
                addNext(this::endTurn, 0);
            } else {
                terminated.set(true);
            }
        }
    }

    protected void decrementCount() {
        this.count--;
        this.apply();
    }

    /**
     * Planifie {@code runnable} dans {@code time} ms.
     * Si le combat est en état curAction/traped, reschedule de façon non-bloquante
     * au lieu du précédent spin-wait qui bloquait le thread de l'executor.
     *
     * @param runnable la tâche à exécuter
     * @param time     délai minimal en ms (0 = dès que possible)
     */
    @Override
    public void addNext(Runnable runnable, int time) {
        if (terminated.get()) return;
        if (this.fight == null || this.fight.isFinish()) {
            terminated.set(true);
            return;
        }

        Fighter current = this.fight.getFighterByOrdreJeu();
        if (this.fight.getState() == Constant.FIGHT_STATE_ACTIVE
                && this.fighter != null
                && current != null
                && current.getId() != this.fighter.getId()) {
            // Tour deja passe: on stoppe proprement cette IA pour eviter les reschedules zombies.
            terminated.set(true);
            return;
        }

        final long scheduledAt = System.nanoTime();
        final int normalizedDelay = Math.max(0, time);

        if (this.fight.isCurAction() || this.fight.isTraped()) {
            // Reschedule non-bloquant : on réessaie dans AIDelay ms sans bloquer le thread
            final int remaining = Math.max(0, time - Config.getInstance().AIDelay);
            long now = System.currentTimeMillis();
            if (blockedSinceMs == 0L)
                blockedSinceMs = now;
            blockedCycles++;
            long blockedForMs = now - blockedSinceMs;

            if (Config.getInstance().AIProfiling) {
                if (blockedCycles == 1 || blockedCycles % 10 == 0) {
                    World.world.logger.info("[AI-PROF] scheduler blocked fight={} fighter={} remaining={} blockedFor={}ms curAction={} traped={}",
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1,
                            remaining,
                            blockedForMs,
                            this.fight.isCurAction(),
                            this.fight.isTraped());
                }
            }

            if (blockedForMs >= STUCK_BLOCK_TIMEOUT_MS) {
                World.world.logger.warn("[AI-PROF] scheduler stuck, force endTurn fight={} fighter={} blockedFor={}ms curAction={} traped={}",
                        this.fight.getId(),
                        this.fighter != null ? this.fighter.getId() : -1,
                        blockedForMs,
                        this.fight.isCurAction(),
                        this.fight.isTraped());
                this.fight.setCurAction(false);
                this.fight.setTraped(false);
                this.stop = true;
                addNext(this::endTurn, 0);
                return;
            }
            POOL.schedule(() -> addNext(runnable, remaining), Config.getInstance().AIDelay, TimeUnit.MILLISECONDS);
        } else {
            blockedSinceMs = 0L;
            blockedCycles = 0;
            POOL.schedule(() -> {
                long waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - scheduledAt);
                if (Config.getInstance().AIProfiling && waitedMs >= Config.getInstance().AIProfilingWarnMs) {
                    World.world.logger.info("[AI-PROF] scheduler wait={}ms requested={}ms fight={} fighter={}",
                            waitedMs,
                            normalizedDelay,
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1);
                }
                runnable.run();
            }, normalizedDelay, TimeUnit.MILLISECONDS);
        }
    }
}
