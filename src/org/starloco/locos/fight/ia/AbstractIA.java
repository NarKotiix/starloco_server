package org.starloco.locos.fight.ia;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
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
            terminated.set(true);
            if (this.fighter.haveInvocation()) {
                // Planifier via le pool — ne pas bloquer le thread courant
                POOL.schedule(() -> this.fight.endTurn(false, this.fighter), 0, TimeUnit.MILLISECONDS);
            } else {
                this.fight.endTurn(false, this.fighter);
            }
        } else {
            if (!this.fight.isFinish()) {
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

        if (this.fight.isCurAction() || this.fight.isTraped()) {
            // Reschedule non-bloquant : on réessaie dans AIDelay ms sans bloquer le thread
            final int remaining = Math.max(0, time - Config.getInstance().AIDelay);
            POOL.schedule(() -> addNext(runnable, remaining), Config.getInstance().AIDelay, TimeUnit.MILLISECONDS);
        } else {
            POOL.schedule(runnable, Math.max(0, time), TimeUnit.MILLISECONDS);
        }
    }
}
