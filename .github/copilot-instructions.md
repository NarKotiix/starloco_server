# Instructions GitHub Copilot pour ce dépôt

## Contexte et architecture

Ce dépôt correspond au serveur StarLoco. L’architecture globale est la suivante (simplifiée) :

- Racine `Server/` :
    - `README.md` : point d’entrée principal de la documentation.
    - `IA100-OPTIMIZATION-INDEX.md` : index de navigation pour l’optimisation IA v1.5.0.
    - `ARCHITECTURE.md` : description de l’architecture globale du projet.
    - `SUMMARY.md` : sommaire / index documentaire.
    - `config.properties`, `build.gradle`, etc. : fichiers techniques racine.

- `COMMITS/` : **HISTORIQUE & VERSIONS**
    - Objectif : historique, changelogs, rapports techniques, analyses d’évolution.
    - `COMMITS/README.md` : navigation de ce dossier.
    - `COMMITS/commits/` : timeline de commits documentés (par ex. `01-deadlock-ia-fix.md`).
    - `COMMITS/releases/` : rapports de versions (par ex. `changelog-ia100-optimization-v1-5-0.md`).
    - `COMMITS/topics/` : analyses techniques détaillées (deep dives).
    - `COMMITS/archive/` : anciens documents (backup / legacy).
    - Fichiers spécifiques : par ex. `ia100-optimization-commit-guide.md`.

- `docs/` : **DOCUMENTATION OPÉRATIONNELLE DURABLE**
    - Objectif : documentation stable et de long terme, guides, configuration, déploiement.
    - `docs/README.md` : navigation de ce dossier.
    - Exemples :
        - `quick-start.md`, `quick-start-logging.md`, `quick-start-star-respawn.md`, `quick-shutdown.md`
        - `configuration-star-respawn.md`, `graceful-shutdown.md`, `maps-classiques-multigroupes.md`
        - `readme-logs.md`, `utf8-accents-fix.md`
        - `ia100-optimization-technical-guide.md` (avant/après code détaillé).

- `src/` : **CODE SOURCE**
    - Par ex. `src/org/starloco/locos/fight/ia/type/IA104.java`, `IA106.java`, `IA107.java`, etc.

- `Logs/` : **LOGS RUNTIME**
    - `server.log`, `errors.log`, répertoires `AIProfiling/`, `CommandAdmin/`, etc.

- `build/` : artefacts de compilation (générés).
- `Outils/` : scripts d’analyse et outils divers (scripts maison).
- `save/` : snapshots de données.
- `gradle/` : wrapper Gradle.

## Principes d’organisation documentaire

### Séparation claire : `docs/` vs `COMMITS/`

- `docs/` = documentation **DURABLE** :
    - Contient : guides d’exploitation, configuration, tutoriels, docs techniques stables.
    - Audience : ops, devs, admins, toute personne qui exploite le serveur.
    - Durée de vie : longue (plusieurs mois/années).
    - Exemple de bons candidats pour `docs/` :
        - guides « quick start »,
        - guides de configuration,
        - guides d’arrêt propre (graceful shutdown),
        - documents techniques durables comme `ia100-optimization-technical-guide.md`.

- `COMMITS/` = **HISTORIQUE & VERSIONS** :
    - Contient : changelogs, rapports de version, analyses d’évolution, docs de phase.
    - Audience : développeurs, tech leads, “historiens” du projet.
    - Durée de vie : archivée mais consultable pour traçabilité.
    - Structure :
        - `COMMITS/commits/` : timeline de changements majeurs.
        - `COMMITS/releases/` : rapports de versions (ex. `changelog-*-vX-Y-Z.md`).
        - `COMMITS/topics/` : deep dives techniques, analyses détaillées.
        - `COMMITS/archive/` : anciens documents obsolètes ou déplacés.

### Règles d’affectation

Créer / mettre à jour dans `docs/` **si** :
- le document reste pertinent > 6 mois ;
- c’est un guide / tutoriel / configuration ;
- c’est opérationnel (“comment faire”) ;
- ça décrit un comportement fonctionnel ou technique stable.

Créer / mettre à jour dans `COMMITS/` **si** :
- c’est un rapport de version (v1.4.0, v1.5.0, etc.) ;
- c’est l’analyse d’une modification majeure ;
- c’est un document historique (“what changed, when, why”) ;
- c’est lié à une phase précise (ex : campagne d’optimisation IA v1.5.0) ;
- c’est un document qui accompagne un ou plusieurs commits.

### Jamais de `.md` à la racine (sauf exceptions contrôlées)

- **INTERDIT** : créer de nouveaux fichiers `.md` à la racine de `Server/` sans instruction explicite.
- Exceptions autorisées à la racine (déjà existantes ou ajoutées de façon contrôlée) :
    - `README.md` (point d’entrée),
    - `ARCHITECTURE.md`,
    - `SUMMARY.md`,
    - `IA100-OPTIMIZATION-INDEX.md` ou autres INDEX globaux validés.
- Si un nouveau document `.md` est nécessaire, il doit être placé soit dans `docs/`, soit dans `COMMITS/` (ou un sous-répertoire adapté), jamais “orphelin” à la racine.

Exemple : pour une future optimisation IA, le modèle attendu est :
- à la racine : un unique fichier index de navigation (par ex. `IA200-OPTIMIZATION-INDEX.md`) si nécessaire ;
- dans `COMMITS/` : changelog(s) de version, guide(s) de commit, analyses d’évolution ;
- dans `docs/` : guide(s) technique(s) avant/après code, guides d’exploitation, etc.

## Emplacement des scripts et outils

- Tous les scripts, outils, utilitaires (shell, PowerShell, batch, Python, etc.) doivent aller dans `Outils/` ou un sous-dossier de `Outils/` approprié.
- **Ne jamais** proposer de créer des scripts directement à la racine, ni à côté des sources (`src/`), sauf demande explicite.
- Si un outil est spécifique à un domaine (par exemple analyse de logs IA, outils DB, outils de build), proposer des sous-dossiers comme :
    - `Outils/logs/`,
    - `Outils/db/`,
    - `Outils/build/`,
    - `Outils/dev/`, etc.

Quand tu proposes un fichier script, indique toujours un chemin de type :
- `Outils/nom-du-script.sh`
- `Outils/logs/analyse-ai-profiling.py`
  et jamais seulement le nom du fichier.

## Comportement attendu : “prépare un commit”

Quand je demande explicitement quelque chose comme :
- « prépare un commit »
- « prépare les commits pour cette session »
- « prépare la documentation de commit »

alors tu dois :

1. **Analyser les changements de la session**
    - Regrouper les modifications par thème logique (feature, bugfix, refactor, perf, etc.).
    - Identifier les fichiers impactés et les risques / impacts.

2. **Proposer un plan de commit**
    - Proposer un ou plusieurs commits logiques (idéalement cohérents et découplés).
    - Pour chaque commit :
        - un titre clair (éventuellement conventionnel : `feat:`, `fix:`, `refactor:`, etc.) ;
        - un message de commit complet (titre + corps).

3. **Générer / mettre à jour la documentation dans `COMMITS/`**
    - Créer ou compléter un ou plusieurs fichiers `.md` dans :
        - `COMMITS/commits/` (pour des commits individuels ou un lot),
        - et/ou `COMMITS/topics/` si c’est une analyse détaillée.
    - Nom de fichier recommandé :
        - `COMMITS/commits/NN-description-courte.md`
          où `NN` est un index ou un identifiant logique,
          ou un format type `YYYY-MM-DD-description.md` si plus adapté.
    - Contenu recommandé :
      ```markdown
      # Résumé
 
      ## Contexte
      - Problème ou objectif initial.
 
      ## Changements
      - Liste des modifications (par fichier / par fonctionnalité).
      - Impacts possibles et comportements modifiés.
 
      ## Tests
      - Types de tests effectués (unitaires, manuels, etc.).
      - Résultats observés.
 
      ## Notes
      - Points de vigilance.
      - Tâches futures / idées d’amélioration.
      ```

4. **Ne jamais faire le push**
    - Me proposer la liste des commandes :
        - `git add ...`
        - `git commit -m "..."` (et éventuellement `git commit -m "..." -m "..."` pour le corps)
    - Ne **jamais** supposer ni déclencher un `git push`.
    - Toujours rappeler que le `push` reste à ma charge.

5. **Lien avec `docs/`**
    - Si les changements impactent des docs durables, suggérer les mises à jour nécessaires dans `docs/` (sans les créer à la racine).
    - Proposer par exemple :
        - “Mettre à jour `docs/quick-start-logging.md` pour refléter la nouvelle configuration.”
        - “Créer un paragraphe supplémentaire dans `docs/ia100-optimization-technical-guide.md` si l’optimisation IA est étendue.”

## Suggestions de RELEASE

Je souhaite que tu me suggères des RELEASES quand cela te semble cohérent, **sans les créer automatiquement**.

Critères indicatifs pour suggérer une RELEASE :
- Plusieurs fonctionnalités significatives ajoutées depuis la dernière version.
- Corrections de bugs critiques ou changements de compatibilité.
- Gros jalon technique (ex. campagne d’optimisation IA, refonte majeure, etc.).

Quand tu estimes qu’une RELEASE est pertinente :
1. Me le signaler clairement, par exemple :
    - « Je recommande de préparer une nouvelle RELEASE (ex : v1.5.0) pour les raisons suivantes : … »
2. Proposer :
    - Un numéro de version candidat (en suivant un schéma cohérent déjà utilisé : v1.2.0, v1.4.1, v1.5.0, etc.).
    - Un **brouillon de changelog** pour `COMMITS/releases/` (par ex. `changelog-<nom>-vX-Y-Z.md`),
      construit à partir :
        - des fichiers dans `COMMITS/commits/`,
        - des analyses dans `COMMITS/topics/`,
        - et éventuellement de docs dans `docs/`.
    - Les commandes git typiques :
        - `git tag -a vX.Y.Z -m "Description..."`,
        - `git push origin vX.Y.Z`.
3. Ne jamais créer la tag ni exécuter de push, simplement les suggérer.

## Style d’interaction

- Toujours respecter la structure existante : `docs/` pour le durable, `COMMITS/` pour l’historique et les versions, `Outils/` pour les scripts.
- Quand tu proposes une nouvelle doc, indiquer explicitement dans quel dossier elle doit aller (`docs/` ou `COMMITS/`) et pourquoi.
- Quand tu produis du code qui implique une doc, rappeler quelles pages de `docs/` ou quels fichiers de `COMMITS/` devraient être mis à jour.
- En cas de conflit entre ces instructions et une demande ponctuelle de ma part dans la conversation, **suivre la demande ponctuelle** mais signaler le conflit pour que je puisse ajuster si nécessaire.

---

## Workflows développeur (Windows / Unix)

- Build :
  - Unix : `./gradlew`
  - Windows : `./gradlew.bat`
  - Rebuild complet : `./gradlew.bat clean build`
  - Jar exécutable généré : `build/libs/Server-1.0.0.jar` (fat jar Gradle utilisé par les scripts de lancement).
- Tests :
  - `./gradlew.bat test` (JUnit 4.13.2, tests dans `src/test/java`).
  - Exemple ciblé : `./gradlew.bat test --tests org.starloco.locos.entity.monster.MobGroupStarProgressionTest`.
- Lancer le serveur :
  - Windows recommandé : `./Start-Server.bat` (UTF-8 + flags JVM + vérif du jar).
  - Unix : `./start-server.sh`.
- Arrêt propre :
  - CTRL+C doit déclencher `Main.stop(...)` et la séquence de sauvegarde du monde (cf. `docs/graceful-shutdown.md`).

## Conventions & contraintes globales

- **Documentation** :
  - Docs durables, guides, config, tutos -> `docs/`.
  - Historique, changelogs, analyses de phase, docs de commit -> `COMMITS/`.
  - Ne **jamais** créer de nouveaux `.md` "perdus" à la racine de `Server/`.
  - Exception actuelle à la racine : `README.md` ; pour tout autre `.md` racine, suivre une demande humaine explicite ou réutiliser un index déjà présent dans la branche courante.
- **Scripts & outils** :
  - Tous les scripts/outils vont dans `Outils/` ou ses sous-dossiers.
  - Ne pas créer de scripts dans : la racine, `src/`, `docs/` ou `COMMITS/`.
- **Code Java** :
  - Respecter la compat Java 8 (source/target 1.8 dans `build.gradle`).
  - Garder le layout de packages `org.starloco.locos.*`.
  - Respecter le layout Gradle effectif : `src/` pour le runtime, `src/resources` pour les ressources embarquées, `src/test/java` pour les tests.
  - Les dépendances runtime sont déclarées dans `build.gradle` via Maven Central (ancien modèle `libs/*.jar` migré).
  - Eviter les refactors massifs opportunistes sans contexte / validation.
- **Sécurité & secrets** :
  - `config.properties` contient des endpoints & credentials : ne jamais les exposer dans les réponses.
  - Attention aux logs et au contenu des fichiers dans `Logs/`.

## Intégration points & zones sensibles

- Fichiers sensibles :
  - `Main.java`, `Config.java`, `Database.java`, `GameServer.java`, `ExchangeClient.java`.
- Logging :
  - Config dans `src/resources/logback.xml`.
  - `src/logback.xml` est conservé pour référence IDE / historique ; le runtime et le jar utilisent `src/resources/logback.xml`.
  - Le niveau global passe par `APP_LOG_LEVEL`, et le logger `ai.profiling` écrit dans `Logs/AIProfiling/ai_profiling.log` sans additivité vers `server.log`.
  - Comportement attendu décrit en partie dans `docs/readme-logs.md`.
- IA & profiling :
  - Timings/behaviors pilotés par des clés `AI_*` dans `Config.java` + `config.properties`.
  - Toute modification d'IA doit tenir compte des performances et des logs d'`AIProfiling/`.
- Persistance étoiles monstres :
  - `World.saveMobGroupStarsSnapshot()` écrit dans `save/mob_group_stars.snapshot` pendant l'arrêt propre.
  - Le format/legacy parsing est couvert par `src/test/java/org/starloco/locos/entity/monster/MobGroupStarProgressionTest.java`.

## Fast onboarding order for Copilot

Pour comprendre rapidement le projet, lire en priorité :
1. `README.md`, `build.gradle`, `config.properties`, `src/org/starloco/locos/kernel/Main.java`, `src/org/starloco/locos/kernel/Config.java`
2. `src/org/starloco/locos/database/Database.java`, `src/org/starloco/locos/game/GameServer.java`, `src/org/starloco/locos/exchange/ExchangeClient.java`, `src/resources/logback.xml`, `Start-Server.bat`
3. Pour la documentation, router systématiquement vers `docs/` (durable) ou `COMMITS/` (historique/version).

## Auto-mise à jour des fichiers d'orientation (AGENTS, Copilot & changelogs)

Pour rester aligné avec la réalité du dépôt, maintenir à jour les fichiers suivants dès qu'un changement les impacte :

- `AGENTS.md`
  - A mettre à jour si le flux de démarrage change, si les workflows dev évoluent, si les conventions de structure changent, ou si de nouveaux points sensibles apparaissent.
- `.github/copilot-instructions.md`
  - A mettre à jour si la séparation `docs/` vs `COMMITS/` évolue, si le workflow "prépare un commit" change, si les conventions de nommage changent, ou si de nouveaux répertoires/pratiques doivent être connus.
- `.github/prompt-files/prepare-commit.prompt.md`
  - A mettre à jour si les étapes de préparation de commit évoluent, si la structure de réponse attendue change, ou si de nouvelles contraintes doivent être prises en compte.
- `.github/quick-navigation.md`
  - A mettre à jour si un fichier d'orientation est ajoute, deplace ou renomme dans `.github/`, si l'ordre de recherche recommande change, ou si un nouveau workflow prompt est ajoute dans `.github/prompt-files/`.
- `COMMITS/releases/changelog-ia100-optimization-v1-5-0.md`
  - A mettre à jour en cas de modification du périmètre IA100 (performance, logique, stabilité, nouvelle étape significative).

Pour chaque changement concerné :
1. Vérifier si `AGENTS.md`, `.github/copilot-instructions.md`, `.github/prompt-files/prepare-commit.prompt.md`, `.github/quick-navigation.md` ou le changelog IA100 sont encore exacts.
2. Proposer les mises à jour nécessaires (sections, lignes, exemples).
3. Si une nouvelle phase d'optimisation ou un jalon majeur apparaît, proposer soit une mise à jour du changelog existant, soit un nouveau changelog dédié (ex. `changelog-ia200-optimization-v1-6-0.md`) si le périmètre dépasse IA100.

