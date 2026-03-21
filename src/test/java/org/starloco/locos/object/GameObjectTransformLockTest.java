package org.starloco.locos.object;

import org.junit.Test;

import static org.junit.Assert.*;

public class GameObjectTransformLockTest {

    @Test
    public void shouldLockTransformOnlyOnceUntilUnlocked() {
        GameObject object = new GameObject(1);

        assertFalse(object.isLockedForTransform());
        assertTrue(object.tryLockForTransform());
        assertTrue(object.isLockedForTransform());
        assertFalse(object.tryLockForTransform());

        object.unlockForTransform();

        assertFalse(object.isLockedForTransform());
        assertTrue(object.tryLockForTransform());
    }
}

