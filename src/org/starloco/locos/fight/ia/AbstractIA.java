package org.starloco.locos.fight.ia;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Constant;
import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Locos on 18/09/2015.
 * Optimized: shared pool (no thread-per-instance), non-blocking addNext, AtomicBoolean terminated flag.
 */
public abstract class AbstractIA implements IA {

    /** Logger dédié pour les logs AIProfiling (écrit dans Logs/AIProfiling/) */
    private static final Logger AIPROF_LOGGER = LoggerFactory.getLogger("ai.profiling");

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
    private long blockedSinceMs = 0L;
    private int blockedCycles = 0;
    private static final long STUCK_BLOCK_TIMEOUT_MS = 2500L;
    private long endTurnPollStartMs = 0L;
    private int endTurnPollCycles = 0;
    private static final long STUCK_ENDTURN_TIMEOUT_MS = 2500L;
    private static final int ENDTURN_POLL_DELAY_MS = 25;
    private static final long ENDTURN_IDLE_GUARD_MS = 500L;
    private static final byte INVOCATION_ACTION_BUDGET = 4;
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    public AbstractIA(Fight fight, Fighter fighter, byte count) {
        this.fight = fight;
        this.fighter = fighter;
        this.count = (fighter != null && fighter.isInvocation() && count > INVOCATION_ACTION_BUDGET)
                ? INVOCATION_ACTION_BUDGET
                : count;
    }

    @Override public Fight getFight()               { return fight;   }
    @Override public Fighter getFighter()           { return fighter; }
    @Override public boolean isStop()               { return stop;    }
    @Override public void setStop(boolean stop)     { this.stop = stop; }

    @Override
    public void endTurn() {
        if (terminated.get()) return;

        if (this.stop && this.fighter != null && !this.fighter.isDead()) {
            // Réinitialise le tracking de polling seulement sur sortie propre
            endTurnPollStartMs = 0L;
            endTurnPollCycles = 0;
            terminated.set(true);
            if (this.fighter.haveInvocation()) {
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
                // CORRECTION DEADLOCK: Vérifier si c'est vraiment le tour de ce fighter
                // Si le tour a changé et ce n'est plus son tour, sortir proprement
                Fighter currentFighter = this.fight.getFighterByOrdreJeu();
                if (currentFighter != null && this.fighter != null && 
                    currentFighter.getId() != this.fighter.getId()) {
                    // Le tour a changé, ce n'est plus notre tour
                    endTurnPollStartMs = 0L;
                    endTurnPollCycles = 0;
                    terminated.set(true);
                    if (Config.getInstance().AIProfiling) {
                        AIPROF_LOGGER.info("[AI-PROF] endTurn exit, turn changed fight={} fighter={} currentFighter={}",
                                this.fight.getId(),
                                this.fighter.getId(),
                                currentFighter.getId());
                    }
                    return;
                }

                long now = System.currentTimeMillis();
                if (endTurnPollStartMs == 0L)
                    endTurnPollStartMs = now;
                endTurnPollCycles++;

                long pollingForMs = now - endTurnPollStartMs;
                if (Config.getInstance().AIProfiling && (endTurnPollCycles == 1 || endTurnPollCycles % 20 == 0)) {
                    AIPROF_LOGGER.info("[AI-PROF] endTurn polling fight={} fighter={} mob={} cycles={} elapsed={}ms stop={}",
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1,
                            fighterMobLabel(this.fighter),
                            endTurnPollCycles,
                            pollingForMs,
                            this.stop);
                }

                if (pollingForMs >= STUCK_ENDTURN_TIMEOUT_MS) {
                    AIPROF_LOGGER.warn("[AI-PROF] endTurn stuck, force pass turn fight={} fighter={} mob={} cycles={} elapsed={}ms",
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1,
                            fighterMobLabel(this.fighter),
                            endTurnPollCycles,
                            pollingForMs);
                    this.stop = true;
                    endTurn();
                    return;
                }

                // Garde ciblée: si rien n'est en cours (ni action ni trap) mais que stop ne bascule jamais,
                // on termine le tour pour éviter le timeout 2500ms qui crée une latence visible.
                if (!this.stop
                        && this.count <= 0
                        && !this.fight.isCurAction()
                        && !this.fight.isTraped()
                        && pollingForMs >= ENDTURN_IDLE_GUARD_MS) {
                    if (Config.getInstance().AIProfiling) {
                        AIPROF_LOGGER.warn("[AI-PROF] endTurn idle guard, force pass turn fight={} fighter={} mob={} cycles={} elapsed={}ms",
                                this.fight.getId(),
                                this.fighter != null ? this.fighter.getId() : -1,
                                fighterMobLabel(this.fighter),
                                endTurnPollCycles,
                                pollingForMs);
                    }
                    this.stop = true;
                    endTurn();
                    return;
                }

                // Évite les boucles ultra-serrées à délai 0 (CPU + cycles explosifs) lors du polling endTurn.
                addNext(this::endTurn, ENDTURN_POLL_DELAY_MS);
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
        final int baseDelay = Config.getInstance().getAIDelay(this.fighter);
        final int remaining = Math.max(0, time - baseDelay);


        if (this.fight.isCurAction() || this.fight.isTraped()) {
            // Reschedule non-bloquant : on réessaie dans AIDelay ms sans bloquer le thread
            long now = System.currentTimeMillis();
            if (blockedSinceMs == 0L)
                blockedSinceMs = now;
            blockedCycles++;
            long blockedForMs = now - blockedSinceMs;

            if (Config.getInstance().AIProfiling) {
                if (blockedCycles == 1 || blockedCycles % 10 == 0) {
                    AIPROF_LOGGER.info("[AI-PROF] scheduler blocked fight={} fighter={} mob={} remaining={} blockedFor={}ms curAction={} traped={}",
                            this.fight.getId(),
                            this.fighter != null ? this.fighter.getId() : -1,
                            fighterMobLabel(this.fighter),
                            remaining,
                            blockedForMs,
                            this.fight.isCurAction(),
                            this.fight.isTraped());
                }
            }

            if (blockedForMs >= STUCK_BLOCK_TIMEOUT_MS) {
                AIPROF_LOGGER.warn("[AI-PROF] scheduler stuck, force endTurn fight={} fighter={} mob={} blockedFor={}ms curAction={} traped={}",
                        this.fight.getId(),
                        this.fighter != null ? this.fighter.getId() : -1,
                        fighterMobLabel(this.fighter),
                        blockedForMs,
                        this.fight.isCurAction(),
                        this.fight.isTraped());
                this.fight.setCurAction(false);
                this.fight.setTraped(false);
                this.stop = true;
                addNext(this::endTurn, 0);
                return;
            }
            POOL.schedule(() -> addNext(runnable, remaining), baseDelay, TimeUnit.MILLISECONDS);
            return;
        }

        blockedSinceMs = 0L;
        blockedCycles = 0;
        POOL.schedule(() -> {
            long waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - scheduledAt);
            if (Config.getInstance().AIProfiling && waitedMs >= Config.getInstance().AIProfilingWarnMs) {
                AIPROF_LOGGER.info("[AI-PROF] scheduler wait={}ms requested={}ms fight={} fighter={} mob={}",
                        waitedMs,
                        normalizedDelay,
                        this.fight.getId(),
                        this.fighter != null ? this.fighter.getId() : -1,
                        fighterMobLabel(this.fighter));
            }
            runnable.run();
        }, normalizedDelay, TimeUnit.MILLISECONDS);
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
        if (fighter.isDouble() && fighter.getDouble() != null) {
            return fighter.getDouble().getName();
        }
        return "type=" + fighter.getType();
    }
}
