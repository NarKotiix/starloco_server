# Starloco-Fun Server (Dofus 1.29+)

**Reprise et optimisation du projet Starloco par [@NarKotiix](https://github.com/NarKotiix)**  
Améliorations : refactoring code, fix UTF-8, optimisations performance (donjons, respawn, Account), build Gradle propre.  
Développé avec aide IA Perplexity / Claude pour debugging et features.

## Auteurs originaux

- [@sarazar928ghost](https://github.com/sarazar928ghost) - Discord: Kevin#6537
- [@arwase](https://github.com/arwase) - Discord: Arwase#6656 (TWEAK Gladiatrool)
- [@iR3SH](https://github.com/iR3SH) - Discord: Hydronish#0843 (FIX Gladiatrool)

## Nouveautés

- Gladiatrool
- Client 1.39.8: Split packets, positions Spells/Obj en Int (pas Hexa)
- Maps chiffrées offi

## Debugs

- **Joueur**: FM cac 100%, anti-multi-équip (ex. 20 anneaux), morph armes, drop équipé, pas d'ID fantôme, IA, panda porter/jeter, items class persistants, ban/mute OK
- **Gladiatrool**: Sauvegarde sorts incarnation/perso (reboot-proof), effets toniques

## Optimisations [@NarKotiix]

- Refactoring massif: lisibilité/pro (mouvements inventaire, actions objets)
- Opti: getDirBetweenTwoCase, addObjet/createNewItem, packets Stats (raccourcis)
- Conditions anti-exceptions config
- Build Gradle 7.4 + JDK 8 (Linux/Windows sync)
- Fix encodage UTF-8 (é/à/ç)

## Téléchargement

- **Server**: Sources + Gradle (build.sh / gradlew build)
- **SQL**: help_game / help_login
- **Client**: [Mega 1.39.8](https://mega.nz/file/3sAljAyR#optHLctMbZWvgsksJhOH2gDNkEo-xpwXbyVTr45Q_50)
- Supprimez config.txt ancien au 1er lancement

## Installation rapide

```bash
# Linux (PikaOS/Debian)
./gradlew clean build
java -jar Server-1.0.0.jar

# Windows
.\gradlew.bat clean build
java -jar Server-1.0.0.jar