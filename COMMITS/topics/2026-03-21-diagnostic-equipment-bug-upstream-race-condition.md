# Diagnostic: Bug d'équipement aléatoire - Race Condition Upstream

**Date**: 2026-03-21  
**Bug**: Lors d'un équipement/changement d'item (ex: drag CAC → slot arme), un item aléatoire apparaît à la place.  
**Cause Identifiée**: Race condition dans la résolution de l'objet déjà équipé (exObj).

---

## Source du problème (Dépôt Upstream StarLoco-Game)

### Code problématique (GameClient.java ligne 5930)

```java
GameObject exObj = this.player.getObjetByPos2(position);//Objet a l'ancienne position
```

La méthode `getObjetByPos2()` dans `Player.java` (lignes 2722-2733) :

```java
//TODO: Delete s'te fonction.
public GameObject getObjetByPos2(int pos) {
    if (pos == Constant.ITEM_POS_NO_EQUIPED)
        return null;

    for (Entry<Integer, GameObject> entry : objects.entrySet()) {
        GameObject obj = entry.getValue();

        if (obj.getPosition() == pos)
            return obj;
    }
    return null;
}
```

**Problème critique**:
- **Pas de synchronisation** sur la Map `objects` (contrairement à `getObjetByPos()` ligne 2705 qui a `synchronized(objects)`).
- Accès direct non-sécurisé pendant qu'un autre thread peut modifier la Map (ajout/retrait d'items).
- Retour aléatoire possible si itération interrompue.

---

## État du Code Local (StarLoco-Fun Server)

### Code appliqué (GameClient.java lignes 6656-6669)

Tu as un correctif **partiel** :

```java
private GameObject getEquippedObjectFromInventory(final int position) {
    if (position == Constant.ITEM_POS_NO_EQUIPED) {
        return null;
    }
    for (GameObject item : this.player.getItems().values()) {
        if (item != null && item.getPosition() == position) {
            return item;
        }
    }
    return null;
}

public synchronized void onMovementEquipUnequipItem(GameObject object, final int position, final int quantity, final boolean sendStats) {
    final GameObject exObj = getEquippedObjectFromInventory(position);//Objet reel deja equipe sur ce slot
    ...
}
```

**Points positifs**:
- ✅ Appel dans une méthode `synchronized` (`onMovementEquipUnequipItem` est `synchronized`).
- ✅ Utilise `getItems()` (inventaire réel) au lieu d'accès direct à `objects`.
- ✅ Vérification `item != null` avant accès.

**Points critiques**:
- ⚠️ `getEquippedObjectFromInventory()` **n'est pas synchronisée** elle-même.
  - Si un autre thread modifie l'inventaire pendant l'itération, la valeur retournée peut être obsolète.
- ⚠️ L'appel `this.player.getItems().values()` itère sans lock protecteur interne.

---

## Scénario d'occurrence du bug

1. **Thread A** : Joueur équipe un CAC → appelle `onMovementEquipUnequipItem(..., ITEM_POS_ARME, ...)`
2. **Thread B** (concurrent) : Modification inventaire → change position d'un objet
3. **Problème**: 
   - `getEquippedObjectFromInventory(ITEM_POS_ARME)` itère sur `getItems().values()`.
   - Pendant l'itération, Thread B ajoute/retire/modifie un objet.
   - Itération échoue ou retourne un objet "intermédiaire" aléatoire.
4. **Résultat** : Un objet aléatoire se retrouve en position ITEM_POS_ARME.

---

## Correctif proposé

### Option 1: Synchroniser la lecture (Recommandée)

```java
private GameObject getEquippedObjectFromInventory(final int position) {
    if (position == Constant.ITEM_POS_NO_EQUIPED) {
        return null;
    }
    synchronized (this.player.getItems()) {
        for (GameObject item : this.player.getItems().values()) {
            if (item != null && item.getPosition() == position) {
                return item;
            }
        }
    }
    return null;
}
```

**Avantage**: Garantit une snapshot cohérente de l'inventaire pendant la recherche.

### Option 2: Utiliser la méthode sécurisée existante (Fallback)

Si `Player.java` expose une méthode synchronisée comme :

```java
public synchronized GameObject getObjetByPos(int pos) {
    if (pos == Constant.ITEM_POS_NO_EQUIPED)
        return null;
    synchronized(objects) {
        for (GameObject gameObject : this.objects.values()) {
            if (gameObject.getPosition() == pos && pos == Constant.ITEM_POS_FAMILIER) {
                if (gameObject.getTxtStat().isEmpty()) return null;
                else if (World.world.getPetsEntry(gameObject.getGuid()) == null) return null;
            }
            if (gameObject.getPosition() == pos) return gameObject;
        }
    }
    return null;
}
```

Modifier `onMovementEquipUnequipItem()` pour utiliser :

```java
final GameObject exObj = this.player.getObjetByPos(position);
```

**Avantage**: Réutilise une méthode existante et testée.

---

## Recommandation

**Appliquer Option 1** (synchroniser `getEquippedObjectFromInventory()`):
- Cible précis : la source réelle du bug.
- Impact local : change juste une méthode private.
- Coût performance : minimal (lock très court).

Le correctif partiel actuel (appel dans méthode synchronized) est une barrière, mais **pas suffisant** car l'itération elle-même reste non-protégée.

---

## Tests de validation

Après application du correctif :

1. **Test manuel** : 
   - Give 1000 items aléatoires.
   - Équipe/déséquipe rapidement le CAC.
   - Vérifier qu'aucun item "parasite" n'apparaît dans les slots d'équipement.

2. **Test de charge** :
   - Lancer plusieurs joueurs en parallèle, rapidement équip/déséquip.
   - Vérifier cohérence inventaire (aucun doublon, aucun objet "égaré").

3. **Vérifier logs** :
   - Pas de erreur d'IndexOutOfBoundsException ou ConcurrentModificationException en logs.

---

## État: À APPLIQUER

Commit suggéré : `fix(equipment): synchronize equipment resolution to prevent race condition`

Fichiers : `src/org/starloco/locos/game/GameClient.java`

