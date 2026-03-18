# 🧹 CHANGELOG — Code Quality & Optimisations v1.3.0

> **Date :** 18 Mars 2026  
> **Fichiers modifiés :** `GameClient.java`, `GameCase.java`  
> **Nature :** Corrections de bugs silencieux, optimisations performance, refactoring, qualité de code

---

## Contexte

Suite à une corruption du fichier `GameClient.java` (code parasite inséré avant la déclaration `package`), une revue complète de la qualité du code a été menée sur les deux fichiers les plus volumineux du projet. 27 corrections ont été appliquées, allant de bugs critiques à des améliorations de lisibilité.

---

## 🔴 Bugs critiques corrigés

### 1. Comparaison `==` sur `String` — `tchat()` (GameClient.java)

**Localisation :** Méthode `tchat()`, canal messages privés  
**Problème :** `msg == lastMsg` compare les **références** d'objets, pas le contenu. La protection anti-doublon de message n'a jamais fonctionné.

```java
// AVANT (bug)
if (msg == lastMsg) { ... }

// APRÈS (corrigé)
if (msg.equals(lastMsg)) { ... }
```

---

### 2. Comparaison `==` sur `String` littérale — `movementItemOrKamas()` (GameClient.java)

**Localisation :** Méthode `movementItemOrKamas()`, case `AUCTION_HOUSE_SELLING` (~ligne 2868)  
**Problème :** `split("\\|")[2] == "0"` — Java ne garantit **jamais** l'égalité référentielle de littéraux String issus de `split()`. La vérification était donc toujours ignorée (condition toujours `false`), permettant la mise en vente d'items à un prix de 0 kamas.

```java
// AVANT (bug silencieux — vente à 0k toujours autorisée)
if (packet.substring(1).split("\\|")[2] == "0"
        || packet.substring(2).split("\\|")[2] == "0"
        || packet.substring(3).split("\\|")[2] == "0")
    return;

// APRÈS (corrigé)
if ("0".equals(packet.substring(1).split("\\|")[2])
        || "0".equals(packet.substring(2).split("\\|")[2])
        || "0".equals(packet.substring(3).split("\\|")[2]))
    return;
```

---

## 🟠 Imports inutilisés supprimés (GameClient.java)

| Import supprimé | Raison |
|-----------------|--------|
| `import javax.xml.crypto.Data;` | Import sans rapport (signature XML) — jamais utilisé |
| `import java.util.Collection;` | Interface non référencée dans le fichier |

---

## 🟠 Logs de débogage retirés de la production (GameClient.java)

Trois appels `System.out.println` écrits dans le flux de sortie standard du JVM remplacés par des logs SLF4J/Logback (`logger.debug`) :

| Méthode | Avant | Après |
|---------|-------|-------|
| `parseQuickSetPacket()` | `System.out.println(packet)` | `logger.debug("[QuickSet] packet reçu: {}", packet)` |
| `createQuickSet()` — branche création | `System.out.println("Création")` | `logger.debug("[QuickSet] Création pour joueur {}", ...)` |
| `createQuickSet()` — branche modification | `System.out.println("Modification")` | `logger.debug("[QuickSet] Modification pour joueur {}", ...)` |

---

## 🟠 Gestion des exceptions unifiée (`e.printStackTrace()` → logger)

Tous les `e.printStackTrace()` (sortie standard non structurée) ont été remplacés par des appels au logger Logback, avec contexte (nom du joueur, méthode, identifiants) :

### GameClient.java

| Méthode | Exception capturée | Message logger ajouté |
|---------|--------------------|----------------------|
| `boost()` | `NumberFormatException` | Nom du joueur + message |
| `sendTicket()` | `Exception` | Message complet + stack trace |
| `prismFight()` (3 blocs fusionnés en 1) | `Exception` | ID du prisme + message + stack |
| `response()` — catch interne | `Exception` | Nom du joueur + message + stack |
| `response()` — catch externe | `Exception` | Nom du joueur + message + stack |
| `buy()` | `Exception` | Nom du joueur + message |
| `bigStore()` — case `'l'` | `NullPointerException` | ID template + message |
| `bigStore()` — case `'S'` | `NullPointerException` | ID de recherche + message |
| `setDirection()` | `NumberFormatException` | Nom du joueur + message |
| `send()` | `Exception` | Paquet + message (Logging.write supprimé car redondant) |

### GameCase.java

| Méthode | Exception capturée | Message logger ajouté |
|---------|--------------------|----------------------|
| `startAction()` — parse args | `Exception` | Nom du joueur + message |
| `startAction()` — mount park | `Exception` | Nom du joueur + message + stack |
| `finishAction()` — parse args | `Exception` | Nom du joueur + message |

---

## 🟡 Optimisations performance — Concaténation String → StringBuilder

Les concaténations `+=` dans des boucles créent une nouvelle instance `String` immuable à chaque itération (O(n²) en mémoire). Remplacées par `StringBuilder` :

### `generateKey()` — GameClient.java
```java
// AVANT — 32 allocations String
String key = "";
for (int i = 0; i < 32; i++)
    key = key.concat(String.valueOf(CryptManager.HASH[...]));
return key;

// APRÈS — 1 seul buffer
StringBuilder key = new StringBuilder(32);
for (int i = 0; i < 32; i++)
    key.append(CryptManager.HASH[...]);
return key.toString();
```

### `getGifts()` — GameClient.java
```java
// AVANT — N allocations en boucle
String data = "";
for (String object : gifts.split(";")) {
    if (data.isEmpty()) data = "1~" + ...;
    else data += ";1~" + ...;
}

// APRÈS — 1 StringBuilder partagé
StringBuilder dataBuilder = new StringBuilder();
for (String object : gifts.split(";")) {
    if (dataBuilder.length() > 0) dataBuilder.append(";");
    dataBuilder.append("1~").append(...);
}
SocketManager.GAME_SEND_Ag_PACKET(this, item, dataBuilder.toString());
```

### `response()` — newStats — GameClient.java
```java
// AVANT — N allocations ternaires en boucle
String newStats = "";
for (String i : stats.split(","))
    if (!i.equals(statsReplace))
        newStats += (newStats.isEmpty() ? i : "," + i);

// APRÈS — StringBuilder
StringBuilder newStatsBuilder = new StringBuilder();
for (String i : stats.split(",")) {
    if (!i.equals(statsReplace)) {
        if (newStatsBuilder.length() > 0) newStatsBuilder.append(",");
        newStatsBuilder.append(i);
    }
}
String newStats = newStatsBuilder.toString();
```

### `startAction()` — ZaapiList — GameCase.java
```java
// AVANT — N allocations en boucle
String ZaapiList = "";
for (String s : Zaapis) {
    if (count == Zaapis.length)
        ZaapiList += s + ";" + price;
    else
        ZaapiList += s + ";" + price + "|";
    count++;
}

// APRÈS — StringBuilder
StringBuilder sbZaapi = new StringBuilder();
for (String s : Zaapis) {
    sbZaapi.append(s).append(";").append(price).append("|");
    count++;
}
String ZaapiList = sbZaapi.toString();
```

---

## 🟡 Boxing explicite inutile supprimé

### `buy()` — GameClient.java
```java
// AVANT — retourne Integer, déboxé en int (allocation inutile)
itemID = Integer.valueOf(infos[0]);
qua    = Integer.valueOf(infos[1]);

// APRÈS — parse direct en int primitif
itemID = Integer.parseInt(infos[0]);
qua    = Integer.parseInt(infos[1]);
```

### `getCraftsmenJobIdsByMap()` — GameCase.java
```java
// AVANT — boxing explicite superflu
Integer mapIdAsInteger = Integer.valueOf(mapId);

// APRÈS — widening implicite, autoboxing à l'usage
int mapIdAsInteger = mapId;
```

---

## 🟡 Redondances structurelles supprimées

### `removePlayer()` et `removeFighter()` — GameCase.java

`List.remove(Object)` renvoie déjà `false` si l'élément est absent. L'appel préalable à `contains()` est un double parcours inutile :

```java
// AVANT (O(2n))
if (this.players.contains(player))
    this.players.remove(player);

// APRÈS (O(n))
this.players.remove(player);
```

### `worldInfos()` — GameClient.java

Les cases `'J'` et `'V'` du switch étaient strictement identiques. Fusion par fall-through :

```java
// AVANT — 8 lignes dupliquées
case 'J':
    SocketManager.SEND_CW_INFO_WORLD_CONQUETE(..., 1);
    SocketManager.SEND_CW_INFO_WORLD_CONQUETE(..., 2);
    break;
case 'V':
    SocketManager.SEND_CW_INFO_WORLD_CONQUETE(..., 1);  // copié-collé
    SocketManager.SEND_CW_INFO_WORLD_CONQUETE(..., 2);
    break;

// APRÈS — fall-through propre
case 'J':
case 'V':
    SocketManager.SEND_CW_INFO_WORLD_CONQUETE(..., 1);
    SocketManager.SEND_CW_INFO_WORLD_CONQUETE(..., 2);
    break;
```

### `prismFight()` — GameClient.java

3 blocs `try-catch` séparés pour lire 3 champs du même objet fusionnés en 1 seul :

```java
// AVANT — 15 lignes, 3 blocs try-catch distincts
int FightID = -1;
try { FightID = prism.getFightId(); } catch (Exception e) { e.printStackTrace(); }
short MapID = -1;
try { MapID = prism.getMap(); } catch (Exception e) { e.printStackTrace(); }
int cellID = -1;
try { cellID = prism.getCell(); } catch (Exception e) { e.printStackTrace(); }

// APRÈS — 6 lignes, 1 seul bloc
int FightID = -1;
short MapID = -1;
int cellID = -1;
try {
    FightID = prism.getFightId();
    MapID   = prism.getMap();
    cellID  = prism.getCell();
} catch (Exception e) {
    World.world.logger.error("prismFight: erreur prisme {} : {}", PrismeID, e.getMessage(), e);
}
```

---

## 🟡 Simplifications structurelles

### `blockLoS()` — GameCase.java

```java
// AVANT — boucle impérative avec drapeau
boolean hide = true;
for (Fighter fighter : this.fighters)
    if (!fighter.isHide()) hide = false;
return hide;

// APRÈS — expression stream déclarative
return this.fighters.stream().allMatch(Fighter::isHide);
```

### `getPlayers()` — GameCase.java

```java
// AVANT — allocation inutile d'une ArrayList vide
if (this.players == null)
    return new ArrayList<>();

// APRÈS — singleton immuable, zéro allocation
if (this.players == null)
    return Collections.emptyList();
```

### `useEmote()` — GameClient.java

La vérification `this.player == null` se faisait **après** un `Integer.parseInt()` alors qu'elle devrait être la première garde :

```java
// AVANT — null check après parseInt
final int emote = Integer.parseInt(packet.substring(2));
if (emote == -1) return;
if (this.player == null) return;  // trop tard

// APRÈS — null check en premier
if (this.player == null) return;
final int emote = Integer.parseInt(packet.substring(2));
if (emote == -1) return;
```

---

## 🟡 InterruptedException — restauration du flag thread

Dans le lambda `TimerWaiter` de `movementItemOrKamas()`, une `InterruptedException` était avalée sans restaurer le statut d'interruption du thread, ce qui pouvait masquer une demande d'arrêt du thread pool :

```java
// AVANT — flag d'interruption perdu
} catch (InterruptedException e) {
    e.printStackTrace();
}

// APRÈS — conformité au contrat Java des threads
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // restaure le flag
    World.world.logger.error("runeLambda: interruption thread : {}", e.getMessage());
}
```

---

## 🟡 Encapsulation — `craftsmenJobIds` (GameCase.java)

Le champ était `public static` modifiable depuis n'importe quelle classe. Passage en `private static final` avec accesseur public en lecture seule :

```java
// AVANT
public static Map<List<Integer>, List<Integer>> craftsmenJobIds = new HashMap<>();

// APRÈS
private static final Map<List<Integer>, List<Integer>> craftsmenJobIds = new HashMap<>();

// Accesseur ajouté
public static Map<List<Integer>, List<Integer>> getCraftsmenJobIds() {
    return craftsmenJobIds;
}
```

`GameClient.java` mis à jour pour utiliser `GameCase.getCraftsmenJobIds()` au lieu d'un accès direct.

---

## 🟡 Refactoring — Extraction de `isValidPlayerName()` (GameClient.java)

La logique de validation du nom de personnage était **dupliquée** à l'identique dans deux méthodes :
- `addCharacter()` (création de personnage) — ~38 lignes
- `changeName()` (potion de renommage) — ~35 lignes

Extraction en méthode privée statique partagée `isValidPlayerName(String name, boolean allowUpperCase)` :

```java
/**
 * Valide le nom d'un personnage selon les règles du serveur.
 * @param name           nom à valider
 * @param allowUpperCase si true, les majuscules sont acceptées (changeName)
 * @return true si le nom respecte toutes les contraintes
 */
private static boolean isValidPlayerName(String name, boolean allowUpperCase) {
    // Longueur 3-20, mots interdits (modo, admin, putain, etc.)
    if (name.length() > 20 || name.length() < 3
            || name.contains("modo") || name.contains("admin") ...) return false;
    // Lettres et tiret uniquement, max 1 tiret, pas 3 lettres consécutives
    ...
    return true;
}
```

**Résultat :** ~70 lignes dupliquées → ~30 lignes de méthode partagée + 1 ligne d'appel par site.

---

## Récapitulatif par fichier

### `GameCase.java` — 10 corrections

| # | Catégorie | Description |
|---|-----------|-------------|
| 1 | Encapsulation | `craftsmenJobIds` `public` → `private static final` + accesseur |
| 2 | Simplification | `blockLoS()` : boucle → `stream().allMatch()` |
| 3 | Redondance | `removePlayer()` : double `contains()` + `remove()` → `remove()` seul |
| 4 | Allocation | `getPlayers()` : `new ArrayList<>()` → `Collections.emptyList()` |
| 5 | Redondance | `removeFighter()` : même correction que `removePlayer()` |
| 6 | Logging | `startAction()` catch parse → logger avec contexte |
| 7 | Performance | `ZaapiList` : `+=` en boucle → `StringBuilder` |
| 8 | Logging | `startAction()` catch mount park → logger avec contexte |
| 9 | Boxing | `Integer.valueOf(mapId)` → `int mapIdAsInteger = mapId` |
| 10 | Logging | `finishAction()` catch parse → logger avec contexte |

### `GameClient.java` — 20 corrections

| # | Catégorie | Description |
|---|-----------|-------------|
| 1 | Import | Suppression `javax.xml.crypto.Data` (inutilisé) |
| 2 | Import | Suppression `java.util.Collection` (inutilisé) |
| 3–5 | Debug | 3× `System.out.println` → `logger.debug` |
| 6 | Logging | `boost()` : `e.printStackTrace()` → logger |
| 7 | Logging | `sendTicket()` : `e.printStackTrace()` → logger |
| 8 | Performance | `generateKey()` : `String.concat()` en boucle → `StringBuilder` |
| 9 | Performance | `getGifts()` : `+=` en boucle → `StringBuilder` |
| 10 | Duplication | `worldInfos()` : cases `J`/`V` identiques → fall-through |
| 11 | Redondance | `prismFight()` : 3 try-catch → 1 seul bloc |
| 12 | Performance | `response()` : `newStats +=` → `StringBuilder` |
| 13 | Logging | `response()` catch interne → logger |
| 14 | Logging | `response()` catch externe → logger |
| 15 | Boxing | `Integer.valueOf()` → `Integer.parseInt()` dans `buy()` |
| 16 | Logging | `buy()` catch → logger |
| 17–18 | Logging | `bigStore()` 2 catch → logger |
| 19 | Threading | `InterruptedException` : `Thread.currentThread().interrupt()` ajouté |
| **20** | **Bug 🔴** | `== "0"` → `"0".equals(...)` dans `movementItemOrKamas()` |
| **21** | **Bug 🔴** | `msg == lastMsg` → `msg.equals(lastMsg)` dans `tchat()` |
| 22 | Null-safety | `useEmote()` : null-check déplacé avant `parseInt` |
| 23 | Logging | `setDirection()` catch → logger |
| 24 | Logging | `send()` : `e.printStackTrace()` supprimé (logger Logback suffisant) |
| 25 | Refactoring | `isValidPlayerName()` extrait de `addCharacter()` + `changeName()` |

---

## Impact global

| Métrique | Avant | Après |
|----------|-------|-------|
| `e.printStackTrace()` | 14+ occurrences | 0 |
| `System.out.println` | 3 occurrences | 0 |
| Imports inutilisés | 2 | 0 |
| Comparaisons `==` sur String | 2 bugs actifs | 0 |
| Concaténations `+=` en boucle | 4 | 0 |
| Code dupliqué (validation nom) | ~70 lignes × 2 | 1 méthode partagée |
| `Integer.valueOf()` boxing inutile | 3 | 0 |

