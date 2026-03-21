# Catalogue IA2-IA107

Documentation durable du role de chaque IA du serveur et de son comportement concret en combat.

## Portee et limites

- Source de verite: `src/org/starloco/locos/fight/ia/IAHandler.java` et `src/org/starloco/locos/fight/ia/type/IA*.java`.
- Ce catalogue couvre les ID IA de 2 a 107.
- Quand un ID n a ni mapping ni fichier, il est marque comme reserve/non implemente.

## Comment lire ce document

- `Budget`: nombre max de cycles de decision (`count`) initialise par `IAHandler`.
- `Base`: classe heritee (`AbstractIA` ou `AbstractNeedSpell`).
- `Comportement concret`: synthese operationnelle deduite des appels utilitaires.

## Pipeline commun du serveur

1. `IAHandler.select(...)` choisit la classe IA selon `mob.getTemplate().getIa()`.
2. `apply()` decide actions (attaque, move, buff, invoc, soin).
3. `AbstractIA.addNext(...)` orchestre le scheduling et les gardes anti-blocage.
4. `Fight` execute: `onFighterDeplace(...)`, `tryCastSpell(...)`, `canCastSpell1(...)`.

## Catalogue detaille IA2-IA107

| IA | Etat | Fichier | Role (IAHandler) | Base | Budget | Comportement concret |
|---:|---|---|---|---|---:|---|
| 2 | implantee | `src/org/starloco/locos/fight/ia/type/IA2.java` | IA Dragonnet rouge | AbstractNeedSpell | 6 | buff/debuff, fuite/kiting |
| 3 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 4 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 5 | implantee | `src/org/starloco/locos/fight/ia/type/IA5.java` | IA Bloqueuse : Avancer vers ennemis | AbstractIA | 5 | rush cac |
| 6 | implantee | `src/org/starloco/locos/fight/ia/type/IA6.java` | IA type invocations (Coffre animé) | AbstractIA | 5 | soin, buff/debuff, invocation, fuite/kiting, jeu equipe |
| 7 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 8 | implantee | `src/org/starloco/locos/fight/ia/type/IA8.java` | IA Surpuissante : Invocation, Buff, Fuite | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting |
| 9 | implantee | `src/org/starloco/locos/fight/ia/type/IA9.java` | IA La Fourbe : Attaque[], Fuite | AbstractIA | 4 | fuite/kiting |
| 10 | implantee | `src/org/starloco/locos/fight/ia/type/IA10.java` | IA Tonneau : Attaque[], Soin si Etat portée | AbstractIA | 8 | soin |
| 11 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 12 | implantee | `src/org/starloco/locos/fight/ia/type/IA12.java` | IA Tofus | AbstractNeedSpell | 4 | fuite/kiting |
| 13 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 14 | implantee | `src/org/starloco/locos/fight/ia/type/IA14.java` | IA Tonneau : Attaque[], Soin si Etat portée | AbstractIA | 8 | invocation, rush cac |
| 15 | mappee sans fichier | - | IA BASIQUE buff sois meme,attaque,pm,attaque,pm | - | 4 | mapping present mais classe IA manquante |
| 16 | implantee | `src/org/starloco/locos/fight/ia/type/IA16.java` | IA Tanu : Tape, va vers l'ennemis, invocation | AbstractNeedSpell | 8 | buff/debuff, invocation |
| 17 | implantee | `src/org/starloco/locos/fight/ia/type/IA17.java` | IA KIMBO | AbstractNeedSpell | 4 | invocation, bond/engage |
| 18 | implantee | `src/org/starloco/locos/fight/ia/type/IA18.java` | Disciple Kimbo | AbstractIA | 4 | fuite/kiting |
| 19 | implantee | `src/org/starloco/locos/fight/ia/type/IA19.java` | IA Des Tynril | AbstractIA | 4 | soin, rush cac, teleportation tactique, jeu equipe |
| 20 | implantee | `src/org/starloco/locos/fight/ia/type/IA20.java` | IA Kaskargo | AbstractIA | 4 | rush cac, teleportation tactique |
| 21 | implantee | `src/org/starloco/locos/fight/ia/type/IA21.java` | IA Krala | AbstractIA | 4 | buff/debuff, invocation |
| 22 | implantee | `src/org/starloco/locos/fight/ia/type/IA22.java` | IA Rasboul | AbstractIA | 4 | invocation, fuite/kiting, teleportation tactique |
| 23 | implantee | `src/org/starloco/locos/fight/ia/type/IA23.java` | IA Rasboul mineur | AbstractIA | 3 | soin, rush cac, jeu equipe |
| 24 | implantee | `src/org/starloco/locos/fight/ia/type/IA24.java` | IA Sac animé | AbstractIA | 3 | buff/debuff, fuite/kiting, rush cac, jeu equipe |
| 25 | implantee | `src/org/starloco/locos/fight/ia/type/IA25.java` | IA Sacrifier | AbstractNeedSpell | 4 | attaque/deplacement standard |
| 26 | implantee | `src/org/starloco/locos/fight/ia/type/IA26.java` | IA Kitsou | AbstractIA | 4 | buff/debuff, invocation, fuite/kiting, rush cac |
| 27 | implantee | `src/org/starloco/locos/fight/ia/type/IA27.java` | IA BASIQUE attaque,pm,attaque,pm | AbstractNeedSpell | 4 | invocation, placement puis cast |
| 28 | implantee | `src/org/starloco/locos/fight/ia/type/IA28.java` | IA sphincter cell | AbstractIA | 4 | invocation, rush cac, teleportation tactique |
| 29 | implantee | `src/org/starloco/locos/fight/ia/type/IA29.java` | IA Tortu | AbstractIA | 4 | buff/debuff, rush cac |
| 30 | implantee | `src/org/starloco/locos/fight/ia/type/IA30.java` | - | AbstractNeedSpell | 4 | buff/debuff, invocation, placement puis cast, jeu equipe |
| 31 | implantee | `src/org/starloco/locos/fight/ia/type/IA31.java` | rats degoutant | AbstractIA | 3 | rush cac |
| 32 | implantee | `src/org/starloco/locos/fight/ia/type/IA32.java` | IA ARCHER attaque,pm loin d'enemie,attaque,pmvers enemie | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting |
| 33 | implantee | `src/org/starloco/locos/fight/ia/type/IA33.java` | IA BASIQUE buff allier,attaque,pm,attaque,pm | AbstractNeedSpell | 4 | buff/debuff, invocation, jeu equipe |
| 34 | implantee | `src/org/starloco/locos/fight/ia/type/IA34.java` | IA GLOUTO attaque tout le monde ,pm,attaque attaque tout le monde,pm | AbstractNeedSpell | 4 | invocation, degats de zone |
| 35 | implantee | `src/org/starloco/locos/fight/ia/type/IA35.java` | IA BASIQUE ABraknyde heal sois meme,attaque,pm,attaque,pm | AbstractNeedSpell | 4 | soin, buff/debuff, invocation |
| 36 | implantee | `src/org/starloco/locos/fight/ia/type/IA36.java` | IA BASIQUE attaque,Bond,pm,attaque,pm | AbstractNeedSpell | 4 | buff/debuff, invocation, bond/engage |
| 37 | implantee | `src/org/starloco/locos/fight/ia/type/IA37.java` | IA BASIQUE Branche soignante heal amis,attaque,pm,attaque,pm | AbstractNeedSpell | 4 | soin, invocation |
| 38 | implantee | `src/org/starloco/locos/fight/ia/type/IA38.java` | IA BASIQUE buffallier si pas denemie a porter,attaque,pm,attaque,pm | AbstractNeedSpell | 4 | buff/debuff, invocation, jeu equipe |
| 39 | implantee | `src/org/starloco/locos/fight/ia/type/IA39.java` | IA Corbac aprivoiser attaque,pm en ligne de vue droite,attaque,pm fuite | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting |
| 40 | implantee | `src/org/starloco/locos/fight/ia/type/IA40.java` | IA Buveur et momie koalak buff,attaque,pm,attaque,pm | AbstractNeedSpell | 4 | buff/debuff |
| 41 | implantee | `src/org/starloco/locos/fight/ia/type/IA41.java` | IA Wobot | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting |
| 42 | implantee | `src/org/starloco/locos/fight/ia/type/IA42.java` | IA Gonflable | AbstractNeedSpell | 6 | soin, fuite/kiting |
| 43 | implantee | `src/org/starloco/locos/fight/ia/type/IA43.java` | IA Bloqueuse | AbstractNeedSpell | 4 | attaque/deplacement standard |
| 44 | implantee | `src/org/starloco/locos/fight/ia/type/IA44.java` | IA Chaton ecaflip | AbstractNeedSpell | 4 | buff/debuff, jeu equipe |
| 45 | implantee | `src/org/starloco/locos/fight/ia/type/IA45.java` | IA | AbstractNeedSpell | 6 | buff/debuff, invocation, fuite/kiting |
| 46 | implantee | `src/org/starloco/locos/fight/ia/type/IA46.java` | IA lapino | AbstractNeedSpell | 6 | soin, buff/debuff, fuite/kiting, jeu equipe |
| 47 | implantee | `src/org/starloco/locos/fight/ia/type/IA47.java` | IA coffre animer | AbstractNeedSpell | 4 | buff/debuff, fuite/kiting |
| 48 | implantee | `src/org/starloco/locos/fight/ia/type/IA48.java` | IA Sanglier | AbstractNeedSpell | 4 | invocation |
| 49 | implantee | `src/org/starloco/locos/fight/ia/type/IA49.java` | IA Chaferfu lancier | AbstractNeedSpell | 6 | buff/debuff, degats de zone |
| 50 | implantee | `src/org/starloco/locos/fight/ia/type/IA50.java` | IA Gourlo le terrible | AbstractNeedSpell | 6 | buff/debuff, invocation, rush cac |
| 51 | implantee | `src/org/starloco/locos/fight/ia/type/IA51.java` | IA Workette | AbstractNeedSpell | 6 | buff/debuff, jeu equipe |
| 52 | implantee | `src/org/starloco/locos/fight/ia/type/IA52.java` | IA avance Heal et buff allier plus fuite | AbstractNeedSpell | 6 | soin, buff/debuff, fuite/kiting, jeu equipe |
| 53 | implantee | `src/org/starloco/locos/fight/ia/type/IA53.java` | IA Peki Peki invisible apres 3 attaque et fuite | AbstractNeedSpell | 8 | buff/debuff, fuite/kiting |
| 54 | implantee | `src/org/starloco/locos/fight/ia/type/IA54.java` | IA Bworkmage | AbstractNeedSpell | 8 | invocation, fuite/kiting |
| 55 | implantee | `src/org/starloco/locos/fight/ia/type/IA55.java` | IA dopeul feca | AbstractNeedSpell | 8 | buff/debuff, fuite/kiting |
| 56 | implantee | `src/org/starloco/locos/fight/ia/type/IA56.java` | IA Chene mou | AbstractNeedSpell | 8 | rush cac, placement puis cast |
| 57 | implantee | `src/org/starloco/locos/fight/ia/type/IA57.java` | IA dopeul Osamodas | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 58 | implantee | `src/org/starloco/locos/fight/ia/type/IA58.java` | IA rn | AbstractNeedSpell | 8 | soin |
| 59 | implantee | `src/org/starloco/locos/fight/ia/type/IA59.java` | IA dopeul enutrof (temple 59-68) | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting |
| 60 | implantee | `src/org/starloco/locos/fight/ia/type/IA60.java` | IA dopeul sram | AbstractNeedSpell | 8 | buff/debuff, invocation |
| 61 | implantee | `src/org/starloco/locos/fight/ia/type/IA61.java` | IA dopeul xelor | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting |
| 62 | implantee | `src/org/starloco/locos/fight/ia/type/IA62.java` | IA dopeul ecflip | AbstractNeedSpell | 8 | buff/debuff, invocation |
| 63 | implantee | `src/org/starloco/locos/fight/ia/type/IA63.java` | IA dopeul eniripsa | AbstractNeedSpell | 8 | soin, buff/debuff, invocation |
| 64 | implantee | `src/org/starloco/locos/fight/ia/type/IA64.java` | IA dopeul iop | AbstractNeedSpell | 8 | buff/debuff, invocation, bond/engage |
| 65 | implantee | `src/org/starloco/locos/fight/ia/type/IA65.java` | IA dopeul cra | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting |
| 66 | implantee | `src/org/starloco/locos/fight/ia/type/IA66.java` | IA dopeul sadida | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting |
| 67 | implantee | `src/org/starloco/locos/fight/ia/type/IA67.java` | IA dopeul Sacrieur | AbstractNeedSpell | 8 | buff/debuff, invocation |
| 68 | implantee | `src/org/starloco/locos/fight/ia/type/IA68.java` | IA dopeul pandawa | AbstractNeedSpell | 8 | buff/debuff, invocation |
| 69 | implantee | `src/org/starloco/locos/fight/ia/type/IA69.java` | IA Trooll | AbstractNeedSpell | 8 | invocation |
| 70 | implantee | `src/org/starloco/locos/fight/ia/type/IA70.java` | Element Spark AI | AbstractNeedSpell | 8 | buff/debuff |
| 71 | implantee | `src/org/starloco/locos/fight/ia/type/IA71.java` | Regenerative AI with anti-summon clause | AbstractNeedSpell | 8 | soin, buff/debuff, invocation |
| 72 | implantee | `src/org/starloco/locos/fight/ia/type/IA72.java` | Healing AI with anti-summon clause with only close-combat damage spells and abolition | AbstractNeedSpell | 5 | soin, buff/debuff, invocation |
| 73 | implantee | `src/org/starloco/locos/fight/ia/type/IA73.java` | Bomberfu AI | AbstractNeedSpell | 8 | buff/debuff |
| 74 | implantee | `src/org/starloco/locos/fight/ia/type/IA74.java` | Osamodas dopple summon AI | AbstractNeedSpell | 8 | buff/debuff, fuite/kiting, jeu equipe |
| 75 | implantee | `src/org/starloco/locos/fight/ia/type/IA75.java` | Osamodas dopple selfbuff summon AI | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 76 | implantee | `src/org/starloco/locos/fight/ia/type/IA76.java` | Royal Tofu AI | AbstractNeedSpell | 8 | invocation, fuite/kiting, rush cac |
| 77 | implantee | `src/org/starloco/locos/fight/ia/type/IA77.java` | Tofukaz AI | AbstractNeedSpell | 8 | buff/debuff, fuite/kiting |
| 78 | implantee | `src/org/starloco/locos/fight/ia/type/IA78.java` | Tofoone AI | AbstractNeedSpell | 8 | buff/debuff, fuite/kiting, jeu equipe |
| 79 | implantee | `src/org/starloco/locos/fight/ia/type/IA79.java` | Tofurby AI | AbstractNeedSpell | 8 | buff/debuff, invocation, fuite/kiting |
| 80 | implantee | `src/org/starloco/locos/fight/ia/type/IA80.java` | Minotot | AbstractNeedSpell | 8 | buff/debuff, rush cac, placement puis cast |
| 81 | implantee | `src/org/starloco/locos/fight/ia/type/IA81.java` | Minotoror | AbstractNeedSpell | 8 | rush cac, placement puis cast, jeu equipe |
| 82 | implantee | `src/org/starloco/locos/fight/ia/type/IA82.java` | Crocabulia | AbstractNeedSpell | 8 | invocation, rush cac, placement puis cast |
| 83 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 84 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 85 | implantee | `src/org/starloco/locos/fight/ia/type/IA85.java` | Ougah | AbstractNeedSpell | 4 | soin, buff/debuff, invocation, rush cac |
| 86 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 87 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 88 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 89 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 90 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 91 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 92 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 93 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 94 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 95 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 96 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 97 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 98 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 99 | absente | - | ID non mappe dans IAHandler | - | - | non implementee dans le code actuel |
| 100 | implantee | `src/org/starloco/locos/fight/ia/type/IA100.java` | Alpha Invocation Dopeul Féca (+Enu) | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 101 | implantee | `src/org/starloco/locos/fight/ia/type/IA101.java` | Alpha Invocation Dopeul Iop | AbstractNeedSpell | 6 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 102 | implantee | `src/org/starloco/locos/fight/ia/type/IA102.java` | Alpha Invocation Dopeul Cra | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 103 | implantee | `src/org/starloco/locos/fight/ia/type/IA103.java` | Alpha Invocation Dopeul Xélor (+Eni) | AbstractNeedSpell | 4 | soin, buff/debuff, invocation, fuite/kiting, jeu equipe |
| 104 | implantee | `src/org/starloco/locos/fight/ia/type/IA104.java` | Alpha Invocation Dopeul Sadi | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 105 | implantee | `src/org/starloco/locos/fight/ia/type/IA105.java` | Alpha Invocation Dopeul Panda (parti de l'IA 68) | AbstractNeedSpell | 4 | buff/debuff, invocation, jeu equipe |
| 106 | implantee | `src/org/starloco/locos/fight/ia/type/IA106.java` | Alpha Invocation Dopeul Eca | AbstractNeedSpell | 4 | buff/debuff, invocation, fuite/kiting, jeu equipe |
| 107 | implantee | `src/org/starloco/locos/fight/ia/type/IA107.java` | Alpha Invocation MAGE (Compagnon) | AbstractNeedSpell | 4 | soin, buff/debuff, fuite/kiting, jeu equipe |

## Points techniques importants

- Les IA en `AbstractNeedSpell` exploitent des listes de sorts pre-classees (`buffs`, `highests`, `cacs`, `heal`, etc.).
- Les IA en `AbstractIA` utilisent souvent des heuristiques specialisees (boss scriptes, etats, teleports, invocations de mecanique).
- Les IA 100+ sont des IA alpha pour invocations/dopeuls/compagnons et servent de base evolutive recente.

## Fichiers coeur a connaitre

- `src/org/starloco/locos/fight/ia/IAHandler.java`
- `src/org/starloco/locos/fight/ia/AbstractIA.java`
- `src/org/starloco/locos/fight/ia/AbstractNeedSpell.java`
- `src/org/starloco/locos/fight/ia/util/Function.java`
- `src/org/starloco/locos/fight/Fight.java`