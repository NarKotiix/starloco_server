package org.starloco.locos.entity.monster;

public final class MobGroupStarProgression {

    public static final int VISIBLE_UNIT = 20;
    public static final int FIRST_VISIBLE_DELAY_MINUTES = 10;
    public static final int ONE_TO_TEN_VISIBLE_MINUTES = 480;
    public static final int MAX_CAP = 200;

    private MobGroupStarProgression() {
    }

    public static long firstVisibleDelayMillis() {
        return Math.max(1L, FIRST_VISIBLE_DELAY_MINUTES) * 60000L;
    }

    public static long delayPerInternalPointMillis() {
        long oneToTenVisibleMillis = Math.max(1L, ONE_TO_TEN_VISIBLE_MINUTES) * 60000L;
        int remainingInternalPoints = VISIBLE_UNIT * 9;
        return Math.max(1L, oneToTenVisibleMillis / remainingInternalPoints);
    }

    public static long requiredDelayForCurrentInternalStars(int currentInternalStars) {
        if (currentInternalStars < 0 || currentInternalStars >= MAX_CAP) {
            return 0L;
        }
        if (currentInternalStars < VISIBLE_UNIT) {
            return firstVisibleDelayMillis();
        }
        return delayPerInternalPointMillis();
    }

    public static long remainingMillisBeforeNextGain(int currentInternalStars, long lastUpdateAt, long currentTimeMillis) {
        long requiredDelay = requiredDelayForCurrentInternalStars(currentInternalStars);
        if (requiredDelay <= 0L) {
            return 0L;
        }

        long elapsed = Math.max(0L, currentTimeMillis - lastUpdateAt);
        return Math.max(0L, requiredDelay - elapsed);
    }

    public static int toVisibleStars(int internalStars) {
        if (internalStars < 0) {
            return internalStars;
        }
        return clampInternalStars(internalStars) / VISIBLE_UNIT;
    }

    public static int clampInternalStars(int internalStars) {
        if (internalStars < 0) {
            return internalStars;
        }
        return Math.min(MAX_CAP, internalStars);
    }

    public static ProgressResult advance(int currentInternalStars, long lastUpdateAt, long currentTimeMillis) {
        if (currentInternalStars < 0 || currentInternalStars >= MAX_CAP) {
            return new ProgressResult(currentInternalStars, lastUpdateAt, false);
        }

        long elapsed = currentTimeMillis - lastUpdateAt;
        if (elapsed <= 0L) {
            return new ProgressResult(currentInternalStars, lastUpdateAt, false);
        }

        int oldStars = currentInternalStars;
        int newStars = currentInternalStars;
        long consumed = 0L;
        long firstStarMillis = firstVisibleDelayMillis();

        if (newStars < VISIBLE_UNIT) {
            if (elapsed < firstStarMillis) {
                return new ProgressResult(currentInternalStars, lastUpdateAt, false);
            }
            newStars = Math.min(MAX_CAP, VISIBLE_UNIT);
            consumed += firstStarMillis;
            elapsed -= firstStarMillis;
        }

        long delayPerPoint = delayPerInternalPointMillis();
        while (newStars < MAX_CAP && elapsed >= delayPerPoint) {
            elapsed -= delayPerPoint;
            consumed += delayPerPoint;
            newStars++;
        }

        return new ProgressResult(newStars, lastUpdateAt + consumed, newStars != oldStars);
    }

    public static Snapshot parseSnapshot(String value, long fallbackLastUpdateAt) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            if (!trimmed.contains(",")) {
                int stars = Integer.parseInt(trimmed);
                return new Snapshot(clampInternalStars(stars), fallbackLastUpdateAt);
            }

            String[] parts = trimmed.split(",", 2);
            int stars = Integer.parseInt(parts[0].trim());
            long lastUpdateAt = Long.parseLong(parts[1].trim());
            return new Snapshot(clampInternalStars(stars), lastUpdateAt);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static final class ProgressResult {
        private final int internalStars;
        private final long lastUpdateAt;
        private final boolean changed;

        public ProgressResult(int internalStars, long lastUpdateAt, boolean changed) {
            this.internalStars = internalStars;
            this.lastUpdateAt = lastUpdateAt;
            this.changed = changed;
        }

        public int getInternalStars() {
            return internalStars;
        }

        public long getLastUpdateAt() {
            return lastUpdateAt;
        }

        public boolean isChanged() {
            return changed;
        }
    }

    public static final class Snapshot {
        private final int internalStars;
        private final long lastUpdateAt;

        public Snapshot(int internalStars, long lastUpdateAt) {
            this.internalStars = clampInternalStars(internalStars);
            this.lastUpdateAt = lastUpdateAt;
        }

        public int getInternalStars() {
            return internalStars;
        }

        public long getLastUpdateAt() {
            return lastUpdateAt;
        }

        public String serialize() {
            return internalStars + "," + lastUpdateAt;
        }
    }
}

