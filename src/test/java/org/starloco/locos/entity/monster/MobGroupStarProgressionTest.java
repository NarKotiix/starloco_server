package org.starloco.locos.entity.monster;

import org.junit.Test;

import static org.junit.Assert.*;

public class MobGroupStarProgressionTest {

    @Test
    public void shouldKeepZeroStarsBeforeTenMinutes() {
        MobGroupStarProgression.ProgressResult result = MobGroupStarProgression.advance(0, 0L, 599999L);

        assertFalse(result.isChanged());
        assertEquals(0, result.getInternalStars());
        assertEquals(0L, result.getLastUpdateAt());
    }

    @Test
    public void shouldReachOneVisibleStarExactlyAtTenMinutes() {
        MobGroupStarProgression.ProgressResult result = MobGroupStarProgression.advance(0, 0L, 600000L);

        assertTrue(result.isChanged());
        assertEquals(20, result.getInternalStars());
        assertEquals(600000L, result.getLastUpdateAt());
        assertEquals(1, MobGroupStarProgression.toVisibleStars(result.getInternalStars()));
    }

    @Test
    public void shouldGainOneInternalPointEvery160SecondsAfterFirstVisibleStar() {
        long perPointDelay = MobGroupStarProgression.delayPerInternalPointMillis();
        MobGroupStarProgression.ProgressResult result = MobGroupStarProgression.advance(20, 600000L, 600000L + perPointDelay);

        assertEquals(160000L, perPointDelay);
        assertTrue(result.isChanged());
        assertEquals(21, result.getInternalStars());
        assertEquals(600000L + perPointDelay, result.getLastUpdateAt());
    }

    @Test
    public void shouldReachTenVisibleStarsAfterEightHoursPlusTenMinutesTotal() {
        long total = MobGroupStarProgression.firstVisibleDelayMillis()
                + MobGroupStarProgression.delayPerInternalPointMillis() * (MobGroupStarProgression.MAX_CAP - MobGroupStarProgression.VISIBLE_UNIT);

        MobGroupStarProgression.ProgressResult result = MobGroupStarProgression.advance(0, 0L, total);

        assertTrue(result.isChanged());
        assertEquals(MobGroupStarProgression.MAX_CAP, result.getInternalStars());
        assertEquals(total, result.getLastUpdateAt());
        assertEquals(10, MobGroupStarProgression.toVisibleStars(result.getInternalStars()));
        assertEquals(490L, total / 60000L);
    }

    @Test
    public void shouldNotExceedCap() {
        MobGroupStarProgression.ProgressResult result = MobGroupStarProgression.advance(199, 0L, 999999999L);

        assertTrue(result.isChanged());
        assertEquals(200, result.getInternalStars());
        assertTrue(result.getLastUpdateAt() > 0L);

        MobGroupStarProgression.ProgressResult capped = MobGroupStarProgression.advance(200, result.getLastUpdateAt(), result.getLastUpdateAt() + 999999999L);
        assertFalse(capped.isChanged());
        assertEquals(200, capped.getInternalStars());
    }

    @Test
    public void shouldSerializeAndParseSnapshotsIncludingLegacyFormat() {
        MobGroupStarProgression.Snapshot snapshot = new MobGroupStarProgression.Snapshot(87, 123456789L);
        MobGroupStarProgression.Snapshot parsed = MobGroupStarProgression.parseSnapshot(snapshot.serialize(), 0L);
        MobGroupStarProgression.Snapshot legacy = MobGroupStarProgression.parseSnapshot("42", 777L);

        assertNotNull(parsed);
        assertEquals(87, parsed.getInternalStars());
        assertEquals(123456789L, parsed.getLastUpdateAt());

        assertNotNull(legacy);
        assertEquals(42, legacy.getInternalStars());
        assertEquals(777L, legacy.getLastUpdateAt());
    }
}

