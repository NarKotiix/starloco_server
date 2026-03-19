# CHANGELOG ADMIN / IA / DB — v1.4.0

> Date : 19 Mars 2026
> Portée : commandes admin, optimisation IA d'invocations, runtime MySQL

---

## Résumé

Cette mise à jour regroupe 4 axes principaux :

1. **`RELOADITEM` fiable côté client**
2. **Commande `SPAWN` simplifiée pour les admins**
3. **Profiling + accélération des invocations IA**
4. **Correction runtime du driver MySQL**

---

## 1) Commande `RELOADITEM`

### Problème corrigé

Les templates d'items étaient bien rechargés en mémoire, mais les clients connectés continuaient souvent à afficher les anciennes informations.

### Correctif appliqué

- vidage du cache `ObjTemplates` avant rechargement,
- rechargement complet depuis `item_template`,
- notification des joueurs connectés sur les objets équipés via paquet de mise à jour.

### Fichier impacté

- `src/org/starloco/locos/game/world/World.java`

### Usage admin

```text
RELOADITEM
```

---

## 2) Commande `SPAWN` simplifiée

### Nouveau comportement

La commande accepte maintenant **deux formats** :

#### Format historique

```text
SPAWN 1295,120,200;1295,120,200
```

- `id,min,max`
- un grade est choisi aléatoirement parmi les niveaux disponibles dans l'intervalle

#### Nouveau format simple

```text
SPAWN 1295,1295
```

- chaque `id` est converti automatiquement en `id,min,max`
- `min/max` sont calculés à partir des grades disponibles en base
- le groupe spawn sur la cellule courante de l'admin (ou cellule libre de fallback si la cellule n'est pas valide)

### Améliorations

- message d'erreur plus explicite si aucun groupe n'est créé,
- retour réel succès/échec côté `GameMap.spawnGroupOnCommand(...)`,
- prise en charge des IDs invalides sans faire échouer tout le groupe.

### Fichiers impactés

- `src/org/starloco/locos/command/CommandAdmin.java`
- `src/org/starloco/locos/area/map/GameMap.java`

### Commande utile de debug

```text
LINEM 1295
```

Affiche les niveaux disponibles pour le monstre ciblé.

---

## 3) IA d'invocations : debug et optimisation

### Objectif

Diagnostiquer les invocations très lentes en combat, puis réduire leur temps total de tour sans changer leur logique fonctionnelle.

### Instrumentation ajoutée

Nouvelle classe :

- `src/org/starloco/locos/fight/ia/IAProfiler.java`

Mesures disponibles :

- `turn.total`
- `IAHandler.apply`
- `Function.getNearestEnnemy`
- `Function.getLowHpEnnemyList`
- `Function.getBestSpellForTarget`

### Logs générés

```text
[AI-PROF] summary metric=turn.total calls=12 avg=3409229us max=5169595us
[AI-PROF] slow turn=4002ms fight=1 fighter=-100 invoc=true reason=turn.end
```

### Optimisations low-risk appliquées

- file d'exécution IA partagée (`ScheduledExecutorService`) au lieu d'un thread par IA,
- reschedule non bloquant dans `AbstractIA.addNext(...)`,
- réduction des délais artificiels dédiés aux invocations,
- optimisation de sélection des ennemis low HP (`O(n²)` -> tri `O(n log n)`),
- suppression de plusieurs parcours inutiles sur les chemins,
- correction du calcul de coût `G` dans `AstarPathfinding`.

### Configuration disponible

```ini
AI_PROFILING=true
AI_PROFILING_INVOCATIONS_ONLY=true
AI_PROFILING_WARN_MS=60
AI_DELAY=100
AI_SPELL_MAX_DELAY=600
AI_INVOCATION_DELAY=50
AI_INVOCATION_SPELL_MAX_DELAY=220
AI_INVOCATION_MOVEMENT_BASE_DELAY=220
AI_INVOCATION_MOVEMENT_STEP_DELAY=55
```

### Fichiers impactés

- `src/org/starloco/locos/fight/Fight.java`
- `src/org/starloco/locos/fight/ia/AbstractIA.java`
- `src/org/starloco/locos/fight/ia/IAHandler.java`
- `src/org/starloco/locos/fight/ia/IAProfiler.java`
- `src/org/starloco/locos/fight/ia/util/Function.java`
- `src/org/starloco/locos/fight/ia/util/AstarPathfinding.java`
- `src/org/starloco/locos/kernel/Config.java`

---

## 4) Correctif runtime MySQL

### Problème observé

En runtime, certaines opérations BD levaient :

```text
java.lang.NoClassDefFoundError: com/mysql/cj/protocol/a/NullValueEncoder
```

### Cause probable

Conflit ou incompatibilité de runtime autour du driver MySQL utilisé avec la pile Java 8 / Hikari historique du projet.

### Correctif appliqué

- verrouillage du driver à une version stable Java 8,
- exclusion de l'ancien artefact `mysql:mysql-connector-java`,
- forçage de `com.mysql:mysql-connector-j:8.0.33` dans Gradle.

### Fichier impacté

- `build.gradle`

---

## Recommandations d'exploitation

### Rebuild

```powershell
cd "H:\server_dofus\Starloco-Fun\Server"
.\gradlew.bat build -x test
```

### Redémarrage

```powershell
.\Start-Server.bat
```

### Vérification du profiling IA

```powershell
Get-Content ".\Logs\server.log" -Tail 300 | Select-String -Pattern "AI-PROF" -SimpleMatch
```

### Vérification des erreurs MySQL

```powershell
Select-String -Path ".\Logs\**\*.log" -Pattern "NullValueEncoder","NoClassDefFoundError"
```

---

## Impact utilisateur

- les reloads d'items sont cohérents côté client,
- les admins peuvent spawn rapidement des groupes avec un input minimal,
- les invocations jouent plus vite et sont profilables,
- les erreurs runtime liées au driver MySQL sont stabilisées.

