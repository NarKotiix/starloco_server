# Navigation interne Copilot (.github)

## Objectif

Ce fichier sert d'index rapide pour savoir ou chercher l'information dans `.github/` avant de repondre ou d'editer.

## Ordre de lecture recommande

1. `copilot-instructions.md`
2. `prompt-files/prepare-commit.prompt.md`
3. `quick-navigation.md` (ce fichier, pour verifier le parcours)

## Quel fichier consulter selon le besoin

- Besoin de regles globales (structure `docs/` vs `COMMITS/`, scripts, style de reponse) : `copilot-instructions.md`
- Besoin du workflow detaille "prepare un commit" : `prompt-files/prepare-commit.prompt.md`
- Besoin de retrouver rapidement la bonne source : `quick-navigation.md`

## Parcours rapide par cas frequent

- Demande "prepare un commit" :
  1. Lire `prompt-files/prepare-commit.prompt.md`
  2. Appliquer les regles de `copilot-instructions.md`
  3. Produire la doc dans `COMMITS/` (jamais a la racine)

- Demande de creation de documentation :
  1. Verifier la destination (`docs/` durable vs `COMMITS/` historique)
  2. Confirmer les conventions dans `copilot-instructions.md`

- Demande de script/outillage :
  1. Confirmer la regle d'emplacement dans `copilot-instructions.md`
  2. Placer les scripts dans `Outils/` ou sous-dossiers adaptes

## Maintenance

Mettre a jour ce fichier quand :

- un fichier d'orientation est ajoute/deplace/renomme dans `.github/`;
- un nouveau prompt est ajoute dans `.github/prompt-files/`;
- l'ordre de recherche recommande change.

