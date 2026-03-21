# Fix: Rapid Equip/Unequip Bug - Object Reference Staleness

**Date**: 2026-03-21  
**Issue**: Double-clicking items rapidly (equip then unequip) causes wrong items to appear in equipment slots.  
**Example**: Équip bottes → double-click déséquip → ressource aléatoire s'affiche à la place.

---

## Root Cause Analysis

### Scenario: Rapid Double-Click Equip/Unequip

```
Timeline:
T0: User double-clicks bottes (equip -> unequip rapid fire)
T0+ FIRST CALL: onMovementEquipUnequipItem(bottes, ITEM_POS_PIEDS, ...)
    - Line 6672: exObj = getEquippedObjectFromInventory(ITEM_POS_PIEDS) 
    - Snapshot: exObj = bottes original
    - Then modifies: bottes.setPosition(ITEM_POS_NO_EQUIPED)
    
T0++ SECOND CALL arrives (while first still processing):
T0++ onMovementEquipUnequipItem(bottes, ITEM_POS_NO_EQUIPED, ...)
    - Line 6672: exObj = getEquippedObjectFromInventory(ITEM_POS_NO_EQUIPED)
    - Expected: bottes (now in NO_EQUIPED from previous call)
    - PROBLEM: It reads the stale reference from T0+
```

### The Bug

Line 6683-6686 (original code):
```java
if(exObj != null) {
    if (exObj.getGuid() != object.getGuid() && exObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
        this.onMovementUnEquipObject(exObj);
}
```

**Problem**: 
- `exObj` is fetched ONCE at method start.
- But `exObj.getPosition()` can **change during method execution** due to concurrent `setPosition()` calls.
- The condition `exObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED` can become FALSE after position changes by another concurrent operation.
- This causes unpredictable item selection in `onMovementUnEquipObject()`.

---

## Solution: GUID Snapshot & Re-Validation

Instead of relying on object reference, use **GUID snapshot** and **re-fetch from current inventory state**:

```java
// Store GUID snapshot to handle rapid equip/unequip without reference staleness
final GameObject exObj = getEquippedObjectFromInventory(position);
final int exObjGuid = exObj != null ? exObj.getGuid() : -1;

// ...

if(exObj != null && exObjGuid != -1) {
    // Re-check that the equipped object is still there and hasn't changed
    // due to concurrent rapid clicks
    final GameObject currentExObj = this.player.getItems().get(exObjGuid);
    if (currentExObj != null && currentExObj.getPosition() == position 
            && exObjGuid != object.getGuid() && currentExObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED) {
        this.onMovementUnEquipObject(currentExObj);
    }
}
```

**Key improvements**:
1. Store initial `exObjGuid` snapshot at method start.
2. Before unequipping, **re-fetch** via `getItems().get(exObjGuid)` to get current state.
3. Validate position **again** to ensure it hasn't changed due to concurrent operations.
4. Only unequip if ALL conditions are still met.

---

## Testing

To reproduce & verify fix:
1. **Give yourself 2 pairs of boots** (or any stackable item).
2. **Equip first pair** to slot.
3. **Double-click rapidly** (equip -> unequip in quick succession).
4. **Observe**:
   - ❌ **Before fix**: Random resource appears instead of boots.
   - ✅ **After fix**: Boots appear correctly in inventory/equipped slot.
5. **Repeat** 10+ times to ensure stability.

---

## Files Modified

- `src/org/starloco/locos/game/GameClient.java` (lines 6671-6697)
  - Method: `onMovementEquipUnequipItem()`
  - Added GUID snapshot and re-validation logic.

---

## Compilation

✅ BUILD SUCCESSFUL

