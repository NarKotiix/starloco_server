# Reference des fichiers IA (`src/org/starloco/locos/fight/ia/type`)

Documentation durable orientee fichier pour comprendre ce que fait chaque IA au coeur du serveur.

## Portee

- Cette page couvre tous les fichiers `IA*.java` presents dans `src/org/starloco/locos/fight/ia/type`.
- Le role d execution est deduit du mapping dans `src/org/starloco/locos/fight/ia/IAHandler.java`.
- Le comportement concret est synthetise a partir des appels utilitaires dans `apply()`.

## Lecture rapide

- `Base`: `AbstractIA` (heuristique brute) ou `AbstractNeedSpell` (logique basee sur familles de sorts).
- `IDs maps`: IDs de template IA qui pointent vers le fichier dans `IAHandler`.
- `Budget`: nombre max de cycles de decision (`count`) par tour.

## Inventaire par fichier

### `IA2.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA2.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 2
- **Budget**: 6
- **Role**: IA Dragonnet rouge
- **Comportement concret**: buff/debuff, fuite/kiting
- **Appels utilitaires clefs**: buffIfPossible, calculInfluence, getNearestEnnemy, getNearestEnnemyNotListedLos, getSummoner, moveFarIfPossible, moveautourIfPossible, moveenfaceIfPossible

### `IA5.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA5.java`
- **Base**: `AbstractIA`
- **IDs maps**: 5
- **Budget**: 5
- **Role**: IA Bloqueuse : Avancer vers ennemis
- **Comportement concret**: rush cac
- **Appels utilitaires clefs**: getNearestEnnemy, moveNearIfPossible

### `IA6.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA6.java`
- **Base**: `AbstractIA`
- **IDs maps**: 6
- **Budget**: 5
- **Role**: IA type invocations (Coffre animé)
- **Comportement concret**: soin, buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossibleAll, buffIfPossible, getNearestEnnemy, getNearestFriend, invocIfPossible, moveFarIfPossible

### `IA8.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA8.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 8
- **Budget**: 4
- **Role**: IA Surpuissante : Invocation, Buff, Fuite
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: buffIfPossible, getNearestEnnemy, getNearestInvocnbrcasemax, invocIfPossibleloin, moveFarIfPossible

### `IA9.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA9.java`
- **Base**: `AbstractIA`
- **IDs maps**: 9
- **Budget**: 4
- **Role**: IA La Fourbe : Attaque[], Fuite
- **Comportement concret**: fuite/kiting
- **Appels utilitaires clefs**: getNearestEnnemy, moveFarIfPossible, moveToAttackIfPossible

### `IA10.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA10.java`
- **Base**: `AbstractIA`
- **IDs maps**: 10
- **Budget**: 8
- **Role**: IA Tonneau : Attaque[], Soin si Etat portée
- **Comportement concret**: soin
- **Appels utilitaires clefs**: HealIfPossible, getNearestEnnemy, moveToAttackIfPossible

### `IA12.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA12.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 12
- **Budget**: 4
- **Role**: IA Tofus
- **Comportement concret**: fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, movediagIfPossible

### `IA14.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA14.java`
- **Base**: `AbstractIA`
- **IDs maps**: 14
- **Budget**: 8
- **Role**: IA Tonneau : Attaque[], Soin si Etat portée
- **Comportement concret**: invocation, rush cac
- **Appels utilitaires clefs**: getNearestEnnemy, invocIfPossible, moveNearIfPossible, moveToAttackIfPossible

### `IA16.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA16.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 16
- **Budget**: 8
- **Role**: IA Tanu : Tape, va vers l'ennemis, invocation
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA17.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA17.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 17
- **Budget**: 4
- **Role**: IA KIMBO
- **Comportement concret**: invocation
- **Appels utilitaires clefs**: attackBondIfPossible, attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossibleKimbo, moveautourIfPossible

### `IA18.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA18.java`
- **Base**: `AbstractIA`
- **IDs maps**: 18
- **Budget**: 4
- **Role**: Disciple Kimbo
- **Comportement concret**: fuite/kiting
- **Appels utilitaires clefs**: attackIfPossibleDiscipleimpair, attackIfPossibleDisciplepair, moveFarIfPossible

### `IA19.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA19.java`
- **Base**: `AbstractIA`
- **IDs maps**: 19
- **Budget**: 4
- **Role**: IA Des Tynril
- **Comportement concret**: soin, rush cac, teleportation tactique, jeu equipe
- **Appels utilitaires clefs**: HealIfPossiblefriend, attackIfPossibleTynril, getNearestEnnemy, getNearestFriend, moveNearIfPossible, moveautourIfPossible, tpIfPossibleTynril

### `IA20.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA20.java`
- **Base**: `AbstractIA`
- **IDs maps**: 20
- **Budget**: 4
- **Role**: IA Kaskargo
- **Comportement concret**: rush cac, teleportation tactique
- **Appels utilitaires clefs**: attackIfPossibleKaskargo, getNearestEnnemynbrcasemax, moveNearIfPossible, tpIfPossibleKaskargo

### `IA21.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA21.java`
- **Base**: `AbstractIA`
- **IDs maps**: 21
- **Budget**: 4
- **Role**: IA Krala
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: buffIfPossibleKrala, invoctantaIfPossible

### `IA22.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA22.java`
- **Base**: `AbstractIA`
- **IDs maps**: 22
- **Budget**: 4
- **Role**: IA Rasboul
- **Comportement concret**: invocation, fuite/kiting, teleportation tactique
- **Appels utilitaires clefs**: IfPossibleRasboulvulner, getNearestEnnemy, ifCanAttack, invocIfPossible, moveFarIfPossible, moveautourIfPossible, tpIfPossibleRasboul

### `IA23.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA23.java`
- **Base**: `AbstractIA`
- **IDs maps**: 23
- **Budget**: 3
- **Role**: IA Rasboul mineur
- **Comportement concret**: soin, rush cac, jeu equipe
- **Appels utilitaires clefs**: HealIfPossible, getNearestFriendNoInvok, moveNearIfPossible

### `IA24.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA24.java`
- **Base**: `AbstractIA`
- **IDs maps**: 24
- **Budget**: 3
- **Role**: IA Sac animé
- **Comportement concret**: buff/debuff, fuite/kiting, rush cac, jeu equipe
- **Appels utilitaires clefs**: buffIfPossible, getNearestFriendNoInvok, moveFarIfPossible, moveNearIfPossible

### `IA25.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA25.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 25
- **Budget**: 4
- **Role**: IA Sacrifier
- **Comportement concret**: attaque/deplacement standard
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA26.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA26.java`
- **Base**: `AbstractIA`
- **IDs maps**: 26
- **Budget**: 4
- **Role**: IA Kitsou
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, rush cac
- **Appels utilitaires clefs**: attackIfPossibleAll, buffIfPossibleKitsou, getNearestEnnemy, invocIfPossible, moveFarIfPossible, moveNearIfPossible

### `IA27.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA27.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 27
- **Budget**: 4
- **Role**: IA BASIQUE attaque,pm,attaque,pm
- **Comportement concret**: invocation, placement puis cast
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveToAttackIfPossible2, moveautourIfPossible

### `IA28.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA28.java`
- **Base**: `AbstractIA`
- **IDs maps**: 28
- **Budget**: 4
- **Role**: IA sphincter cell
- **Comportement concret**: invocation, rush cac, teleportation tactique
- **Appels utilitaires clefs**: TPIfPossiblesphinctercell, attackIfPossiblesphinctercell, getLowHpEnnemyList, getNearestEnnemynbrcasemax, moveNearIfPossible, tryTurtleInvocation

### `IA29.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA29.java`
- **Base**: `AbstractIA`
- **IDs maps**: 29
- **Budget**: 4
- **Role**: IA Tortu
- **Comportement concret**: buff/debuff, rush cac
- **Appels utilitaires clefs**: buffIfPossibleTortu, getNearestEnnemy, moveNearIfPossible

### `IA30.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA30.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 1, 15, 30
- **Budget**: 4
- **Role**: IA BASIQUE attaque,pm,attaque,pm | IA BASIQUE buff sois meme,attaque,pm,attaque,pm
- **Comportement concret**: buff/debuff, invocation, placement puis cast, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveToAttackIfPossible2, moveautourIfPossible

### `IA31.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA31.java`
- **Base**: `AbstractIA`
- **IDs maps**: 31
- **Budget**: 3
- **Role**: rats degoutant
- **Comportement concret**: rush cac
- **Appels utilitaires clefs**: attackIfPossiblerat, getNearestEnnemy, getNearestEnnemynbrcasemax, moveNearIfPossible

### `IA32.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA32.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 32
- **Budget**: 4
- **Role**: IA ARCHER attaque,pm loin d'enemie,attaque,pmvers enemie
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movediagIfPossible

### `IA33.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA33.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 33
- **Budget**: 4
- **Role**: IA BASIQUE buff allier,attaque,pm,attaque,pm
- **Comportement concret**: buff/debuff, invocation, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA34.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA34.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 34
- **Budget**: 4
- **Role**: IA GLOUTO attaque tout le monde ,pm,attaque attaque tout le monde,pm
- **Comportement concret**: invocation, degats de zone
- **Appels utilitaires clefs**: attackAllIfPossible, getNearestAllnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA35.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA35.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 35
- **Budget**: 4
- **Role**: IA BASIQUE ABraknyde heal sois meme,attaque,pm,attaque,pm
- **Comportement concret**: soin, buff/debuff, invocation
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossibleloin, moveautourIfPossible

### `IA36.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA36.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 36
- **Budget**: 4
- **Role**: IA BASIQUE attaque,Bond,pm,attaque,pm
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackBondIfPossible, attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA37.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA37.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 37
- **Budget**: 4
- **Role**: IA BASIQUE Branche soignante heal amis,attaque,pm,attaque,pm
- **Comportement concret**: soin, invocation
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA38.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA38.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 38
- **Budget**: 4
- **Role**: IA BASIQUE buffallier si pas denemie a porter,attaque,pm,attaque,pm
- **Comportement concret**: buff/debuff, invocation, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA39.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA39.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 39
- **Budget**: 8
- **Role**: IA Corbac aprivoiser attaque,pm en ligne de vue droite,attaque,pm fuite
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearEnnemylignenbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, moveenfaceIfPossible

### `IA40.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA40.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 40
- **Budget**: 4
- **Role**: IA Buveur et momie koalak buff,attaque,pm,attaque,pm
- **Comportement concret**: buff/debuff
- **Appels utilitaires clefs**: attackIfPossible, attackIfPossibleBuveur, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA41.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA41.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 41
- **Budget**: 4
- **Role**: IA Wobot
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, attackIfPossibleWobot, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movediagIfPossible

### `IA42.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA42.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 42
- **Budget**: 6
- **Role**: IA Gonflable
- **Comportement concret**: soin, fuite/kiting
- **Appels utilitaires clefs**: HealIfPossible, getSummoner, moveFarIfPossible, moveautourIfPossible, pmgongon

### `IA43.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA43.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 43
- **Budget**: 4
- **Role**: IA Bloqueuse
- **Comportement concret**: attaque/deplacement standard
- **Appels utilitaires clefs**: getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA44.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA44.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 44
- **Budget**: 4
- **Role**: IA Chaton ecaflip
- **Comportement concret**: buff/debuff, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, attackIfPossiblevisee, buffIfPossible, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA45.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA45.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 45
- **Budget**: 6
- **Role**: IA
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, moveautourIfPossible

### `IA46.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA46.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 46
- **Budget**: 6
- **Role**: IA lapino
- **Comportement concret**: soin, buff/debuff, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: HealIfPossible, buffIfPossible, getNearestAminoinvocnbrcasemax, getNearestFriend, getNearestinvocateurnbrcasemax, moveFarIfPossible, moveautourIfPossible

### `IA47.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA47.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 47
- **Budget**: 4
- **Role**: IA coffre animer
- **Comportement concret**: buff/debuff, fuite/kiting
- **Appels utilitaires clefs**: buffIfPossible, moveFarIfPossible

### `IA48.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA48.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 48
- **Budget**: 4
- **Role**: IA Sanglier
- **Comportement concret**: invocation
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemymurnbrcasemax, invocIfPossible, moveIfPossiblecontremur

### `IA49.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA49.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 49
- **Budget**: 6
- **Role**: IA Chaferfu lancier
- **Comportement concret**: buff/debuff, degats de zone
- **Appels utilitaires clefs**: attackAllIfPossible, buffIfPossible, getNearestAllnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA50.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA50.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 50
- **Budget**: 6
- **Role**: IA Gourlo le terrible
- **Comportement concret**: buff/debuff, invocation, rush cac
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossibleCroca, moveNearIfPossible, moveautourIfPossible, movecacIfPossible

### `IA51.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA51.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 51
- **Budget**: 6
- **Role**: IA Workette
- **Comportement concret**: buff/debuff, jeu equipe
- **Appels utilitaires clefs**: attackIfPossibleBuveur, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA52.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA52.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 52
- **Budget**: 6
- **Role**: IA avance Heal et buff allier plus fuite
- **Comportement concret**: soin, buff/debuff, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: HealIfPossible, buffIfPossible, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, moveautourIfPossible

### `IA53.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA53.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 53
- **Budget**: 8
- **Role**: IA Peki Peki invisible apres 3 attaque et fuite
- **Comportement concret**: buff/debuff, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, attackIfPossiblePeki, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, moveautourIfPossible

### `IA54.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA54.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 54
- **Budget**: 8
- **Role**: IA Bworkmage
- **Comportement concret**: invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, moveautourIfPossible

### `IA55.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA55.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 55
- **Budget**: 8
- **Role**: IA dopeul feca
- **Comportement concret**: buff/debuff, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, attackIfPossibleglyph, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, moveenfaceIfPossible

### `IA56.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA56.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 56
- **Budget**: 8
- **Role**: IA Chene mou
- **Comportement concret**: rush cac, placement puis cast
- **Appels utilitaires clefs**: getNearestEnnemy, getNearestEnnemynbrcasemax, ifCanAttack, ifCanAttackWithSpell, ifCanMove, moveNearIfPossible, moveToAttackIfPossible2, movecacIfPossible

### `IA57.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA57.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 57
- **Budget**: 8
- **Role**: IA dopeul Osamodas
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossibleloin, moveFarIfPossible, moveautourIfPossible

### `IA58.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA58.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 58
- **Budget**: 8
- **Role**: IA rn
- **Comportement concret**: soin
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, attackIfPossibleRN, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA59.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA59.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 59
- **Budget**: 8
- **Role**: IA dopeul enutrof (temple 59-68)
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movediagIfPossible

### `IA60.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA60.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 60
- **Budget**: 8
- **Role**: IA dopeul sram
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA61.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA61.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 61
- **Budget**: 8
- **Role**: IA dopeul xelor
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movediagIfPossible

### `IA62.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA62.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 62
- **Budget**: 8
- **Role**: IA dopeul ecflip
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA63.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA63.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 63
- **Budget**: 8
- **Role**: IA dopeul eniripsa
- **Comportement concret**: soin, buff/debuff, invocation
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA64.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA64.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 64
- **Budget**: 8
- **Role**: IA dopeul iop
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackBondIfPossible, attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA65.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA65.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 65
- **Budget**: 8
- **Role**: IA dopeul cra
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movediagIfPossible

### `IA66.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA66.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 66
- **Budget**: 8
- **Role**: IA dopeul sadida
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movediagIfPossible

### `IA67.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA67.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 67
- **Budget**: 8
- **Role**: IA dopeul Sacrieur
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA68.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA68.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 68
- **Budget**: 8
- **Role**: IA dopeul pandawa
- **Comportement concret**: buff/debuff, invocation
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA69.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA69.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 69
- **Budget**: 8
- **Role**: IA Trooll
- **Comportement concret**: invocation
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA70.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA70.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 70
- **Budget**: 8
- **Role**: Element Spark AI
- **Comportement concret**: buff/debuff
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA71.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA71.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 71
- **Budget**: 8
- **Role**: Regenerative AI with anti-summon clause
- **Comportement concret**: soin, buff/debuff, invocation
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA72.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA72.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 72
- **Budget**: 5
- **Role**: Healing AI with anti-summon clause with only close-combat damage spells and abolition
- **Comportement concret**: soin, buff/debuff, invocation
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA73.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA73.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 73
- **Budget**: 8
- **Role**: Bomberfu AI
- **Comportement concret**: buff/debuff
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveautourIfPossible

### `IA74.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA74.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 74
- **Budget**: 8
- **Role**: Osamodas dopple summon AI
- **Comportement concret**: buff/debuff, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, moveToAttackIfPossible, movediagIfPossible

### `IA75.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA75.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 75
- **Budget**: 8
- **Role**: Osamodas dopple selfbuff summon AI
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossibleloin, moveFarIfPossible, moveautourIfPossible

### `IA76.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA76.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 76
- **Budget**: 8
- **Role**: Royal Tofu AI
- **Comportement concret**: invocation, fuite/kiting, rush cac
- **Appels utilitaires clefs**: attackIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, movecacIfPossible, movediagIfPossible

### `IA77.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA77.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 77
- **Budget**: 8
- **Role**: Tofukaz AI
- **Comportement concret**: buff/debuff, fuite/kiting
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, moveautourIfPossible

### `IA78.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA78.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 78
- **Budget**: 8
- **Role**: Tofoone AI
- **Comportement concret**: buff/debuff, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAllnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, getNearestFriend, moveFarIfPossible, moveautourIfPossible

### `IA79.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA79.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 79
- **Budget**: 8
- **Role**: Tofurby AI
- **Comportement concret**: buff/debuff, invocation, fuite/kiting
- **Appels utilitaires clefs**: buffIfPossible, invocIfPossible, moveFarIfPossible

### `IA80.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA80.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 80
- **Budget**: 8
- **Role**: Minotot
- **Comportement concret**: buff/debuff, rush cac, placement puis cast
- **Appels utilitaires clefs**: attackIfPossibleMinotot, buffIfPossible, buffIfPossibleMinotot, getNearestEnnemy, getNearestEnnemynbrcasemax, ifCanAttack, ifCanAttackWithSpell, ifCanMove

### `IA81.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA81.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 81
- **Budget**: 8
- **Role**: Minotoror
- **Comportement concret**: rush cac, placement puis cast, jeu equipe
- **Appels utilitaires clefs**: getNearestEnnemy, getNearestEnnemynbrcasemax, getNearestFriend, ifCanAttackWithSpell, ifCanMove, moveNearIfPossible, moveToAttackIfPossible2, movecacIfPossible

### `IA82.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA82.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 82
- **Budget**: 8
- **Role**: Crocabulia
- **Comportement concret**: invocation, rush cac, placement puis cast
- **Appels utilitaires clefs**: getNearest, ifCanAttack, ifCanAttackWithSpell, ifCanMove, invocIfPossibleCroca, moveNearIfPossible, moveToAttackIfPossible2, movecacIfPossible

### `IA85.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA85.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 85
- **Budget**: 4
- **Role**: Ougah
- **Comportement concret**: soin, buff/debuff, invocation, rush cac
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossibleAll, buffIfPossible, getLowHpEnnemyList, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveNearIfPossible

### `IA100.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA100.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 100
- **Budget**: 4
- **Role**: Alpha Invocation Dopeul Féca (+Enu)
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, checkIfBuffAvailable, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible

### `IA101.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA101.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 101
- **Budget**: 6
- **Role**: Alpha Invocation Dopeul Iop
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible, moveautourIfPossible

### `IA102.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA102.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 102
- **Budget**: 4
- **Role**: Alpha Invocation Dopeul Cra
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, checkIfBuffAvailable, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible

### `IA103.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA103.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 103
- **Budget**: 4
- **Role**: Alpha Invocation Dopeul Xélor (+Eni)
- **Comportement concret**: soin, buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: HealIfPossible, attackIfPossible, buffIfPossible, checkIfBuffAvailable, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible

### `IA104.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA104.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 104
- **Budget**: 4
- **Role**: Alpha Invocation Dopeul Sadi
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, checkIfBuffAvailable, checkIfInvocPossible, getNearestAminbrcasemax, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax

### `IA105.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA105.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 105
- **Budget**: 4
- **Role**: Alpha Invocation Dopeul Panda (parti de l'IA 68)
- **Comportement concret**: buff/debuff, invocation, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, checkIfBuffAvailable, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveautourIfPossible

### `IA106.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA106.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 106
- **Budget**: 4
- **Role**: Alpha Invocation Dopeul Eca
- **Comportement concret**: buff/debuff, invocation, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: attackIfPossible, buffIfPossible, checkIfBuffAvailable, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, invocIfPossible, moveFarIfPossible

### `IA107.java`
- **Fichier**: `src/org/starloco/locos/fight/ia/type/IA107.java`
- **Base**: `AbstractNeedSpell`
- **IDs maps**: 107
- **Budget**: 4
- **Role**: Alpha Invocation MAGE (Compagnon)
- **Comportement concret**: soin, buff/debuff, fuite/kiting, jeu equipe
- **Appels utilitaires clefs**: HealIfPossible, buffIfPossible, checkIfBuffAvailable, getNearestAminoinvocnbrcasemax, getNearestEnnemy, getNearestEnnemynbrcasemax, moveFarIfPossible, moveautourIfPossible

## Fichiers coeur relies

- `src/org/starloco/locos/fight/ia/IAHandler.java`
- `src/org/starloco/locos/fight/ia/AbstractIA.java`
- `src/org/starloco/locos/fight/ia/AbstractNeedSpell.java`
- `src/org/starloco/locos/fight/ia/util/Function.java`
- `src/org/starloco/locos/fight/Fight.java`