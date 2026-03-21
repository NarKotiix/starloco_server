# Prompt: Préparer un commit pour cette session

Tu es GitHub Copilot et tu travailles dans le dépôt du serveur StarLoco avec l’architecture suivante :
- `docs/` : documentation opérationnelle DURABLE (guides, config, tutoriels, références stables).
- `COMMITS/` : historique & versions (changelogs, analyses, rapports de version, docs de phase).
- `Outils/` : scripts et outils (ne jamais créer de scripts à la racine).
- Jamais de nouveaux fichiers `.md` à la racine sans demande explicite.

Quand j’utilise ce prompt, je veux que tu fasses **uniquement** le travail de préparation de commits et de documentation associée, sans jamais exécuter de commandes git ni supposer que le push est fait.

---

## 1. Analyse de la session

1. Analyse les changements récents au regard du code que je te montre et du contexte de la discussion.
2. Regroupe les modifications en blocs logiques :
    - par fonctionnalité,
    - par bugfix,
    - par refactor / nettoyage,
    - par optimisation de performance,
    - ou autre groupe pertinent.
3. Pour chaque bloc logique, écris un court résumé :
    - ce qui a été modifié,
    - pourquoi,
    - l’impact attendu (fonctionnel ou technique).

Présente d’abord cette vue d’ensemble sous la forme :

- Bloc 1 : [titre court]
    - Description : …
    - Fichiers principaux : …
- Bloc 2 : [titre court]
    - Description : …
    - Fichiers principaux : …
- etc.

---

## 2. Plan de commits

À partir des blocs logiques précédents :

1. Propose un **plan de commits** propre, avec 1 ou plusieurs commits.
2. Pour chaque commit, fournis :
    - un identifiant court : `C1`, `C2`, …
    - un **titre de commit** clair (style Conventional Commit si pertinent, ex. `feat:`, `fix:`, `refactor:`…),
    - un **corps de commit** (quelques lignes) décrivant précisément :
        - le contexte,
        - les principaux changements,
        - les impacts / risques.

Formate le plan ainsi :

- Commit C1
    - Titre : `…`
    - Corps :
        - …
        - …

- Commit C2
    - Titre : `…`
    - Corps :
        - …
        - …

N’écris **pas** encore les commandes git à ce stade.

---

## 3. Documentation dans `COMMITS/`

En respectant l’architecture du dépôt :

1. Pour chaque commit ou groupe de commits significatif, propose un fichier de doc dans `COMMITS/` :

    - Pour un commit individuel ou une petite série :
        - Dossier : `COMMITS/commits/`
        - Nom suggéré :
            - `NN-description-courte.md`
            - ou `YYYY-MM-DD-description-courte.md`
        - Exemple : `03-logging-optimization.md`, `2026-03-21-ia100-fixes.md`

    - Pour une analyse plus large ou un deep dive :
        - Dossier : `COMMITS/topics/`
        - Nom suggéré : `sujet-technique-vX-Y-Z.md` ou autre nom cohérent.

2. Pour chaque fichier suggéré, propose un contenu structuré suivant ce modèle :

```markdown
# Résumé

## Contexte
- Problème ou objectif initial.
- Situation avant les changements.

## Changements
- Liste des modifications par fichier ou par aspect fonctionnel.
- Explication du comportement avant / après quand c’est pertinent.
- Impacts sur les autres parties du projet.

## Tests
- Types de tests effectués (unitaires, manuels, etc.).
- Résultats observés.

## Notes et suivi
- Points de vigilance.
- Prochaines étapes possibles ou travail futur.
