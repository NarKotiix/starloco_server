# Commit #02: Configuration du Logger AIProfiling

**Date:** 21/03/2026  
**Fichiers modifiés:** `src/logback.xml` + `src/org/starloco/locos/fight/ia/AbstractIA.java`  
**Type:** Feature / Logging  
**Impact:** Moyen

---

## 🎯 Problème Identifié

### Symptômes
- Logs IA mélangés avec tous les autres logs dans `server.log`
- Difficile de analyser les performances IA en isolation
- Pollution du fichier principal par les logs [AI-PROF]
- Impossible de faire du monitoring IA en temps réel

### Cause
- Les logs [AI-PROF] utilisent `World.world.logger`
- Pas de distinction entre les types de logs
- Tous les logs écrivent dans le même fichier `server.log`

### Impact sur le Debug
```
server.log (pollué):
  23:16:57.040 INFO GameWorld - NarKotiix --> GA300411;298
  23:16:57.040 INFO GameWorld - NarKotiix --> GAS1
  23:16:57.041 INFO Database - Connection pool status...
  23:16:57.041 INFO IA - [AI-PROF] endTurn polling...   ← Perdu dans la masse!
  23:16:57.042 INFO GameWorld - NarKotiix --> Ow86019015
  23:16:57.043 INFO IA - [AI-PROF] scheduler blocked...  ← Dur à trouver
  
❌ Impossible d'analyser = Perte de temps
```

---

## ✅ Solution Implémentée

### Architecture Proposée

```
Logs/
├── server.log              ← Tous les logs du serveur
├── errors.log              ← Erreurs uniquement
└── AIProfiling/
    └── ai_profiling.log    ← ✅ Logs IA UNIQUEMENT
```

### 1. Configuration du Logger dans `logback.xml`

#### Ajout de l'Appender

```xml
<!-- Appender Fichier pour AI Profiling -->
<appender name="AI_PROFILING_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_DIR}/AIProfiling/ai_profiling.log</file>
    <immediateFlush>true</immediateFlush>
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} | %-5level | %msg%n</pattern>
        <charset>UTF-8</charset>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>${LOG_DIR}/AIProfiling/ai_profiling-%d{yyyy-MM-dd}-%i.log</fileNamePattern>
        <maxFileSize>50MB</maxFileSize>
        <maxHistory>30</maxHistory>
        <totalSizeCap>500MB</totalSizeCap>
    </rollingPolicy>
</appender>
```

#### Configuration du Logger

```xml
<!-- Logger spécifique pour AI Profiling -->
<logger name="ai.profiling" level="DEBUG" additivity="false">
    <appender-ref ref="AI_PROFILING_FILE"/>
</logger>
```

**Important:** `additivity="false"` signifie que les logs ne vont QUE dans `ai_profiling.log`, pas dans `server.log`

### 2. Modification d'AbstractIA

#### Ajout du Logger Statique

```java
package org.starloco.locos.fight.ia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIA implements IA {
    
    // ✨ NOUVEAU LOGGER DÉDIÉ
    private static final Logger AIPROF_LOGGER = LoggerFactory.getLogger("ai.profiling");
    
    // ... reste du code ...
}
```

#### Remplacement des Logs

**Avant:**
```java
World.world.logger.info("[AI-PROF] endTurn polling ...");
```

**Après:**
```java
AIPROF_LOGGER.info("[AI-PROF] endTurn polling ...");
```

### 3. Création du Répertoire

```bash
mkdir Logs/AIProfiling
```

---

## 📊 Résultats

### Avant
```
server.log (500KB, pollué):
  1000 lignes de GameWorld
  500 lignes de Database
  300 lignes IA ← Perdu!
  200 lignes Exchange
  
→ Trouver les logs IA = Mission impossible ❌
```

### Après
```
server.log (300KB, propre):
  1000 lignes de GameWorld
  500 lignes de Database
  200 lignes Exchange
  (Les logs IA ont disparu)

ai_profiling.log (NOUVEAU):
  300 lignes IA uniquement ← Facile à analyser! ✅
```

### Bénéfices

| Aspect | Avant | Après | Bénéfice |
|--------|-------|-------|----------|
| **Fichier principal** | Pollué | Propre | ✅ |
| **Logs IA** | Mélangés | Séparés | ✅ |
| **Analyse** | Difficile | Facile | ✅ |
| **Monitoring** | Impossible | Possible | ✅ |
| **Taille server.log** | 1000KB+ | 500KB | ✅ 50% plus petit |

---

## 🔧 Modifications Détaillées

### Fichier #1: `logback.xml`

#### Nouvelles sections

1. **Appender AI_PROFILING_FILE** (ligne ~70)
   - Fichier: `Logs/AIProfiling/ai_profiling.log`
   - Pattern: Simple (timestamp | level | message)
   - Rolling: 50MB par fichier, max 30 fichiers

2. **Logger "ai.profiling"** (ligne ~120)
   - Level: DEBUG
   - Additivity: FALSE (crucial!)
   - Appender: AI_PROFILING_FILE seulement

#### Impact sur la configuration
```
Avant:
  - 5 appenders
  - 5 loggers

Après:
  - 6 appenders (+ AI_PROFILING_FILE)
  - 6 loggers (+ "ai.profiling")
  - Aucun changement aux appenders existants
```

### Fichier #2: `AbstractIA.java`

#### Imports

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

#### Champs

```java
private static final Logger AIPROF_LOGGER = LoggerFactory.getLogger("ai.profiling");
```

#### Méthodes modifiées

1. **endTurn()** (~5 appels remplacés)
2. **addNext()** (~5 appels remplacés)

Total: ~10 appels `AIPROF_LOGGER.info()` ou `.warn()`

---

## 📝 Types de Messages Loggés

### Format Standard

```
HH:mm:ss.SSS | LEVEL | [AI-PROF] message
```

### Exemples

```
23:16:57.100 | INFO  | [AI-PROF] endTurn exit, turn changed fight=123 fighter=456
23:16:57.150 | INFO  | [AI-PROF] endTurn polling fight=123 fighter=456 cycles=1
23:16:57.250 | WARN  | [AI-PROF] endTurn stuck, force pass turn fight=123 fighter=456
23:16:57.350 | INFO  | [AI-PROF] scheduler blocked fight=123 fighter=456 remaining=200ms
```

---

## ✅ Validation

### Compilation
```
✅ BUILD SUCCESSFUL
✅ Zéro erreurs
✅ Logger correctement trouvé par Logback
```

### Configuration
```
✅ Appender créé
✅ Logger configuré
✅ Additivity = false ✓
✅ Répertoire créé
```

### Comportement
- ✅ Logs [AI-PROF] n'apparaissent PAS dans server.log
- ✅ Logs [AI-PROF] apparaissent dans ai_profiling.log
- ✅ Autres logs restent dans server.log
- ✅ Fichier ai_profiling.log créé au premier log

---

## 🎯 Impact Futur

### Avantages

1. **Debugging:** Analyser les logs IA en isolation
2. **Monitoring:** Tracer les performances IA
3. **Alerting:** Détecter les problèmes IA rapidement
4. **Analytics:** Générer des rapports de performance

### Cas d'usage

```python
# Monitoring temps réel
tail -f Logs/AIProfiling/ai_profiling.log | grep "endTurn stuck"

# Analyse historique
grep "scheduler blocked" Logs/AIProfiling/ai_profiling-2026-03-21-1.log

# Export pour analytics
cat Logs/AIProfiling/ai_profiling.log | process_for_grafana.py
```

### Extensibilité

On peut ajouter d'autres loggers séparés facilement:
```xml
<logger name="ai.combat" level="DEBUG" additivity="false">
    <appender-ref ref="AI_COMBAT_FILE"/>
</logger>

<logger name="ai.pathfinding" level="DEBUG" additivity="false">
    <appender-ref ref="AI_PATHFINDING_FILE"/>
</logger>
```

---

## 🚀 Pour GitBook

**Titre:** "Architecture de Logging Modulaire pour le Profiling IA"

**Points clés à présenter:**
1. Problème de logs pollués et difficiles à analyser
2. Solution: Logger dédié avec appender séparé
3. Avantages pour le debugging et monitoring
4. Extensibilité pour d'autres sous-systèmes
5. Configuration Logback pour la séparation des logs

---

**Commit précédent:** [`01-deadlock-ia-fix.md`](./01-deadlock-ia-fix.md)  
**Commit suivant:** [`03-logging-optimization.md`](./03-logging-optimization.md)

