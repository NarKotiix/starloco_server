# COMMITS

Point d'entrée unique pour l'historique des changements du projet.

L'objectif est de séparer :

- `docs/` → documentation durable d'exploitation, configuration et guides rapides
- `COMMITS/` → historique des changements, changelogs, analyses techniques et dossiers de version

---

## Structure

```text
COMMITS/
├── README.md
├── commits/
│   ├── 01-deadlock-ia-fix.md
│   ├── 02-aiprofiling-logger.md
│   └── 03-logging-optimization.md
├── releases/
│   ├── changelog-admin-ia-db-v1-4-0.md
│   ├── changelog-code-quality-v1-3-0.md
│   ├── changelog-exchange-security-v1-4-1.md
│   ├── changelog-graceful-shutdown-v4-final.md
│   ├── changelog-ia-fix-v1-4-1.md
│   ├── changelog-npc-movement-player-fix-v1-3-1.md
│   ├── changelog-perf-v1-2-0.md
│   ├── changelog-persistence-stars-v1-0-0.md
│   ├── changelog-recent-commits.md
│   ├── changelog-star-respawn-v1-0-0.md
│   ├── index-documentation-v1-2-0.md
│   └── security-hardening-v1-2-1.md
├── topics/
│   ├── guide-correction-ia.md
│   ├── modifications-logging.md
│   ├── modifications-graceful-shutdown.md
│   ├── modifications-star-respawn.md
│   ├── rapport-analyse-ia.md
│   └── ...
└── archive/
    ├── deploiement-rapide.txt
    ├── index-complet.txt
    └── resume-correction.txt
```

---

## Commits récents documentés

### 10. Runtime hardening + maisons Admin
- Fichier : [`commits/10-runtime-hardening-admin-house-2026-03-22.md`](./commits/10-runtime-hardening-admin-house-2026-03-22.md)
- Sujet : lot runtime gameplay/securite + levee de la limite 1 maison pour le groupe `Admin`
- Resultat : comportement maison adapte aux Admins et consolidation de la stabilite runtime

### 9. Outillage DB maps
- Fichier : [`commits/09-db-maps-tooling-session-2026-03-22.md`](./commits/09-db-maps-tooling-session-2026-03-22.md)
- Sujet : pipeline scripts + SQL pour comparer/migrer les dumps de maps
- Resultat : migration maps plus reproductible et mieux tracee

### 1. Correction du deadlock IA
- Fichier : [`commits/01-deadlock-ia-fix.md`](./commits/01-deadlock-ia-fix.md)
- Sujet : combat avec invocation bloqué à ~3100ms
- Résultat : retour à un comportement normal autour de 600-800ms

### 2. Logger dédié AI profiling
- Fichier : [`commits/02-aiprofiling-logger.md`](./commits/02-aiprofiling-logger.md)
- Sujet : séparation des logs IA du `server.log`
- Résultat : logs IA isolés dans `Logs/AIProfiling/`

### 3. Fiabilisation du logging
- Fichier : [`commits/03-logging-optimization.md`](./commits/03-logging-optimization.md)
- Sujet : sécurisation de l'écriture des logs
- Résultat : `immediateFlush=true` + shutdown hook pour limiter toute perte

---

## Releases / changelogs migrés depuis `docs/`

Les documents orientés version, changelog ou historique ont été déplacés dans [`releases/`](./releases/).

Entrées principales :

- [`releases/changelog-admin-ia-db-v1-4-0.md`](./releases/changelog-admin-ia-db-v1-4-0.md)
- [`releases/changelog-exchange-security-v1-4-1.md`](./releases/changelog-exchange-security-v1-4-1.md)
- [`releases/changelog-code-quality-v1-3-0.md`](./releases/changelog-code-quality-v1-3-0.md)
- [`releases/changelog-perf-v1-2-0.md`](./releases/changelog-perf-v1-2-0.md)
- [`releases/security-hardening-v1-2-1.md`](./releases/security-hardening-v1-2-1.md)

---

## Dossiers techniques / analyses migrés depuis `docs/`

Les documents d'analyse détaillée, rapports et variantes d'implémentation ont été déplacés dans [`topics/`](./topics/).

Exemples :

- [`topics/guide-correction-ia.md`](./topics/guide-correction-ia.md)
- [`topics/rapport-analyse-ia.md`](./topics/rapport-analyse-ia.md)
- [`topics/modifications-logging.md`](./topics/modifications-logging.md)
- [`topics/modifications-graceful-shutdown.md`](./topics/modifications-graceful-shutdown.md)

---

## Convention retenue

- `docs/` ne contient plus que les guides stables et opérationnels
- `COMMITS/` centralise tout l'historique projet et les dossiers d'évolution
- un commit important = un fichier dédié dans `COMMITS/commits/`
- les anciennes documentations versionnées sont conservées pour GitBook et archivage

---

## Pour GitBook

Recommandation simple :

1. utiliser ce `README.md` comme sommaire principal
2. présenter `commits/` comme timeline récente
3. présenter `releases/` comme historique de versions
4. garder `topics/` comme annexes techniques

---

**Dernière réorganisation :** 22/03/2026  
**Statut :** propre, centralisé, prêt pour GitBook



