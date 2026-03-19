package org.starloco.locos.area.map;

import org.starloco.locos.area.SubArea;
import org.starloco.locos.area.map.entity.InteractiveDoor;
import org.starloco.locos.client.Player;
//import org.starloco.locos.client.other.Maitre; // La
import org.starloco.locos.client.other.Party;
import org.starloco.locos.database.Database;
import org.starloco.locos.entity.Collector;
import org.starloco.locos.entity.mount.Mount;
import org.starloco.locos.entity.Prism;
import org.starloco.locos.entity.monster.MobGroupStarProgression;
import org.starloco.locos.entity.monster.Monster;
import org.starloco.locos.entity.npc.Npc;
import org.starloco.locos.entity.npc.NpcMovable;
import org.starloco.locos.entity.npc.NpcTemplate;
import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.game.scheduler.Updatable;
import org.starloco.locos.game.world.World;
import org.starloco.locos.area.map.entity.MountPark;
import org.starloco.locos.object.GameObject;
import org.starloco.locos.other.Action;
import org.starloco.locos.common.*;
import org.starloco.locos.kernel.*;
//import org.starloco.locos.util.TimerWaiter;

//import scruffemu.area.map.GameCase;

//import scruffemu.common.SocketManager;

//import org.starloco.locos.common.SocketManager;

//import org.starloco.locos.game.*;
import org.starloco.locos.kernel.Config;

//import scruffemu.common.SocketManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class GameMap {

    private static final short STAR_DEBUG_TRACE_MAP_ID = 1278;

    public static final Map<String, ArrayList<GameObject>> fixMobGroupObjects = new HashMap<>();
    public static final Updatable updatable = new Updatable(1000) {
        private final ArrayList<RespawnGroup> groups = new ArrayList<>();

        @Override
        public void update() {
            if(!this.groups.isEmpty()) {
                long time = System.currentTimeMillis(), random = Formulas.getRandomValue(120000, 300000);

                for(RespawnGroup respawnGroup : new ArrayList<>(this.groups)) {
                    if(respawnGroup.cell != -1) {
                        // Groupes fixes en donjon
                        Map<String, String> data = World.world.getGroupFix(respawnGroup.map.id, respawnGroup.cell);

                        if(time - respawnGroup.lastTime > Long.parseLong(data.get("timer"))) {
                            respawnGroup.map.addStaticGroup(respawnGroup.cell, data.get("groupData"), true);
                            this.groups.remove(respawnGroup);
                        }
                    } else {
                        // Groupes non-fixes en map classique (sans étoiles uniquement)
                        // Délai aléatoire comme avant
                        if(time - respawnGroup.lastTime > random) {
                            respawnGroup.map.spawnGroup(Constant.ALIGNEMENT_NEUTRE, 1, true, -1);
                            this.groups.remove(respawnGroup);
                        }
                    }
                }
            }

            if(this.verify()) {
                if(Config.getInstance().autoReboot) {
                    if (Reboot.check()) {
                        if ((System.currentTimeMillis() - Config.getInstance().startTime) > 60000) {
                            for (Player player : World.world.getOnlinePlayers()) SocketManager.send(player, this.toString());
                            try { Thread.sleep(5000); } catch (Exception ignored) {}
                            Main.stop("Automatic restart");
                        }
                    }
                }

                for (GameMap map : World.world.getMaps()) {
                    if (Config.getInstance().mobGroupMovement) {
                        map.onMapMonsterDeplacement();
                    }
                    map.updateMobGroupsStars();
                    if (map.getMountPark() != null) map.getMountPark().startMoveMounts();
                }

                if (Config.getInstance().npcMovement) {
                    NpcMovable.moveAll();
                }
            }
        }

        @Override
        public ArrayList<RespawnGroup> get() {
            return groups;
        }
    };

    public int nextObjectId = -1;
    public boolean noMarchand = false, noCollector = false, noPrism = false, noTP = false, noDefie = false, noAgro = false, noCanal = false;
    private short id;
    private String date, key, placesStr;
    private byte w, h, X = 0, Y = 0, maxGroup = 3, maxSize, minSize, fixSize;
    private int maxTeam = 0;
    private boolean isMute = false;
    private SubArea subArea;
    private MountPark mountPark;
    private CellCacheImpl cellCache;
    private List<GameCase> cases = new ArrayList<>();
    private GameCase[] casesById = new GameCase[0];
    private List<Fight> fights = new ArrayList<>();
    private ArrayList<Monster.MobGrade> mobPossibles = new ArrayList<>();
    private Map<Integer, Monster.MobGroup> mobGroups = new HashMap<>();
    private Map<Integer, Monster.MobGroup> fixMobGroups = new HashMap<>();
    private Map<Integer, Npc> npcs = new HashMap<>();
    private Map<Integer, ArrayList<Action>> endFightAction = new HashMap<>();
    private Map<Integer, Integer> mobExtras = new HashMap<>();
    // Sauvegarde des étoiles des groupes (pour persistence au redémarrage)
    private Map<Integer, MobGroupStarProgression.Snapshot> savedGroupsStars = new HashMap<>();

    public GameMap(short id, String date, byte w, byte h, String key, String places, String dData, String monsters, String mapPos, byte maxGroup, byte fixSize, byte minSize, byte maxSize, String forbidden, byte sniffed) {
        this.id = id;
        this.date = date;
        this.w = w;
        this.h = h;
        this.key = key;
        this.placesStr = places;
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
        this.cases = World.world.getCryptManager().decompileMapData(this, dData, sniffed);
        this.rebuildCaseIndex();

        try {
            this.maxTeam = extractMaxTeam(places);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            int[] mapInfos = parseMapPos(mapPos);
            this.X = (byte) mapInfos[0];
            this.Y = (byte) mapInfos[1];
            int subArea = mapInfos[2];

            if (subArea == 0 && id == 32) {
                this.subArea = World.world.getSubArea(subArea);
                if (this.subArea != null) this.subArea.addMap(this);
            } else if (subArea != 0) {
                this.subArea = World.world.getSubArea(subArea);
                if (this.subArea != null) this.subArea.addMap(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Main.stop("GameMap1");
        }

        try {
            applyForbidden(forbidden);
        } catch (Exception e) {}

        loadMobPossibles(monsters);
    }

    public GameMap(short id, String date, byte w, byte h, String key, String places) {
        this.id = id;
        this.date = date;
        this.w = w;
        this.h = h;
        this.key = key;
        this.placesStr = places;
        this.cases = new ArrayList<>();
    }

    public GameMap(short id, String date, byte w, byte h, String key,
                   String places, byte x, byte y, byte maxGroup, byte fixSize,
                   byte minSize, byte maxSize) {
        this.id = id;
        this.date = date;
        this.w = w;
        this.h = h;
        this.key = key;
        this.placesStr = places;
        this.X = x;
        this.Y = y;
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
    }

    public static void removeMountPark(int guildId) {
        try {
            World.world.getMountPark().values().stream().filter(park -> park.getGuild() != null).filter(park -> park.getGuild().getId() == guildId).forEach(park -> {
                if (!park.getListOfRaising().isEmpty()) {
                    for (Integer id : new ArrayList<>(park.getListOfRaising())) {
                        if (World.world.getMountById(id) == null) {
                            park.delRaising(id);
                            continue;
                        }
                        World.world.removeMount(id);
                        Database.getDynamics().getMountData().delete(id);
                    }
                    park.getListOfRaising().clear();
                }
                if (!park.getEtable().isEmpty()) {
                    for (Mount mount : new ArrayList<>(park.getEtable())) {
                        if (mount == null) continue;
                        World.world.removeMount(mount.getId());
                        Database.getDynamics().getMountData().delete(mount.getId());
                    }
                    park.getEtable().clear();
                }

                park.setOwner(0);
                park.setGuild(null);
                park.setPrice(3000000);
                Database.getDynamics().getMountParkData().update(park);

                for (Player p : park.getMap().getPlayers())
                    SocketManager.GAME_SEND_Rp_PACKET(p, park);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getObjResist(Player perso, int cellid, int itemID) {
        MountPark MP = perso.getCurMap().getMountPark();
        String packets = "";
        if (MP == null || MP.getObject().size() == 0)
            return 0;
        for (Entry<Integer, Map<Integer, Integer>> entry : MP.getObjDurab().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (cellid == entry.getKey())
                    packets += entry.getKey() + ";" + entry2.getValue() + ";" + entry2.getKey();
            }
        }
        int cell, durability, durabilityMax;
        try {
            String[] infos = packets.split(";");
            cell = Integer.parseInt(infos[0]);
            if (itemID == 7798 || itemID == 7605 || itemID == 7606 || itemID == 7625 || itemID == 7628 || itemID == 7634) {
                durability = Integer.parseInt(infos[1]);
            } else {
                durability = Integer.parseInt(infos[1]) - 1;
            }
            durabilityMax = Integer.parseInt(infos[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        if (durability <= 0) {
            //if (MP.delObject(cell)) {
            durability = 0;
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            SocketManager.SEND_GDO_PUT_OBJECT_MOUNT(perso.getCurMap(), cell
                    + ";" + itemID + ";1;" + durability + ";" + durabilityMax);
            return 0;
            //}
        } else {
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            SocketManager.SEND_GDO_PUT_OBJECT_MOUNT(perso.getCurMap(), cell
                    + ";" + itemID + ";1;" + durability + ";" + durabilityMax);
        }
        return durabilityMax;
    }

    public static int getObjResist(MountPark MP, int cellid, int itemID) {
        String packets = "";
        if (MP == null || MP.getObject().size() == 0)
            return 0;
        for (Entry<Integer, Map<Integer, Integer>> entry : MP.getObjDurab().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (cellid == entry.getKey())
                    packets += entry.getKey() + ";" + entry2.getValue() + ";"
                            + entry2.getKey();
            }
        }
        String[] infos = packets.split(";");
        int cell = Integer.parseInt(infos[0]), durability;
        if (itemID == 7798 || itemID == 7605 || itemID == 7606
                || itemID == 7625 || itemID == 7628 || itemID == 7634) {
            durability = Integer.parseInt(infos[1]);
        } else {
            durability = Integer.parseInt(infos[1]) - 1;
        }
        int durabilityMax = Integer.parseInt(infos[2]);

        if (durability <= 0) {
            //if (MP.delObject(cell)) {
            durability = 0;
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            SocketManager.SEND_GDO_PUT_OBJECT_MOUNT(MP.getMap(), cell
                    + ";" + itemID + ";1;" + durability + ";" + durabilityMax);
            return 0;
            //}
        } else {
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            SocketManager.SEND_GDO_PUT_OBJECT_MOUNT(MP.getMap(), cell + ";"
                    + itemID + ";1;" + durability + ";" + durabilityMax);
        }
        return durabilityMax;
    }

    public void setInfos(String date, String monsters, String mapPos,
                         byte maxGroup, byte fixSize, byte minSize, byte maxSize,
                         String forbidden) {
        this.date = date;
        this.mobPossibles.clear();

        try {
            String[] split = forbidden.split(";");
            noMarchand = split[0].equals("1");
            noCollector = split[1].equals("1");
            noPrism = split[2].equals("1");
            noTP = split[3].equals("1");
            noDefie = split[4].equals("1");
            noAgro = split[5].equals("1");
            noCanal = split[6].equals("1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String mob : monsters.split("\\|")) {
            if (mob.equals(""))
                continue;
            int id1, lvl;
            try {
                id1 = Integer.parseInt(mob.split(",")[0]);
                lvl = Integer.parseInt(mob.split(",")[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (id1 == 0 || lvl == 0)
                continue;
            if (World.world.getMonstre(id1) == null)
                continue;
            if (World.world.getMonstre(id1).getGradeByLevel(lvl) == null)
                continue;
            this.mobPossibles.add(World.world.getMonstre(id1).getGradeByLevel(lvl));
        }
        try {
            String[] mapInfos = mapPos.split(",");
            this.X = Byte.parseByte(mapInfos[0]);
            this.Y = Byte.parseByte(mapInfos[1]);
            int subArea = Integer.parseInt(mapInfos[2]);
            if (subArea == 0 && id == 32) {
                this.subArea = World.world.getSubArea(subArea);
                if (this.subArea != null)
                    this.subArea.addMap(this);
            } else if (subArea != 0) {
                this.subArea = World.world.getSubArea(subArea);
                if (this.subArea != null)
                    this.subArea.addMap(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Main.stop("GameMap2");
        }
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
    }

    public void addMobExtra(Integer id, Integer chances) {
        this.mobExtras.put(id, chances);
    }

    public void setGs(byte maxGroup, byte minSize, byte fixSize, byte maxSize) {
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
    }

    public ArrayList<Monster.MobGrade> getMobPossibles() {
        return this.mobPossibles;
    }

    public void setMobPossibles(String monsters) {
        if (monsters == null || monsters.equals(""))
            return;

        this.mobPossibles = new ArrayList<>();

        for (String mob : monsters.split("\\|")) {
            if (mob.equals(""))
                continue;
            int id1, lvl;
            try {
                id1 = Integer.parseInt(mob.split(",")[0]);
                lvl = Integer.parseInt(mob.split(",")[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (id1 == 0 || lvl == 0)
                continue;
            if (World.world.getMonstre(id1) == null)
                continue;
            if (World.world.getMonstre(id1).getGradeByLevel(lvl) == null)
                continue;
            if (Config.getInstance().HALLOWEEN) {
                switch (id1) {
                    case 98://Tofu
                        if (World.world.getMonstre(794) != null)
                            if (World.world.getMonstre(794).getGradeByLevel(lvl) != null)
                                id1 = 794;
                        break;
                    case 101://Bouftou
                        if (World.world.getMonstre(793) != null)
                            if (World.world.getMonstre(793).getGradeByLevel(lvl) != null)
                                id1 = 793;
                        break;
                }
            }

            this.mobPossibles.add(World.world.getMonstre(id1).getGradeByLevel(lvl));
        }
    }

    public byte getMaxSize() {
        return this.maxSize;
    }

    public byte getMinSize() {
        return this.minSize;
    }

    public byte getFixSize() {
        return this.fixSize;
    }

    public short getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public byte getW() {
        return w;
    }

    public byte getH() {
        return h;
    }

    public String getKey() {
        return key;
    }

    public String getPlaces() {
        return placesStr;
    }

    public void setPlaces(String place) {
        this.placesStr = place;
    }

    public List<GameCase> getCases() {
        return cases;
    }

    private void setCases(List<GameCase> cases) {
        this.cases = cases;
        this.rebuildCaseIndex();
    }

    public GameCase getCase(int id) {
        if (id >= 0 && id < this.casesById.length) {
            return this.casesById[id];
        }
        return null;
    }

    public void removeCase(int id) {
        Iterator<GameCase> iterator = this.cases.iterator();

        while(iterator.hasNext()) {
            GameCase gameCase = iterator.next();
            if(gameCase != null && gameCase.getId() == id) {
                iterator.remove();
                if (id >= 0 && id < this.casesById.length) {
                    this.casesById[id] = null;
                }
                break;
            }
        }
    }

    private void loadMobPossibles(String monsters) {
        if (monsters == null || monsters.isEmpty()) {
            return;
        }

        int start = 0;
        while (start < monsters.length()) {
            int end = monsters.indexOf('|', start);
            if (end == -1) {
                end = monsters.length();
            }
            if (end > start) {
                addMobPossible(monsters, start, end);
            }
            start = end + 1;
        }
    }

    private void addMobPossible(String monsters, int start, int end) {
        int commaIndex = monsters.indexOf(',', start);
        if (commaIndex == -1 || commaIndex >= end) {
            return;
        }

        final int id1;
        final int lvl;
        try {
            id1 = Integer.parseInt(monsters.substring(start, commaIndex));
            lvl = Integer.parseInt(monsters.substring(commaIndex + 1, end));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        int monsterId = id1;
        if (monsterId == 0 || lvl == 0) {
            return;
        }

        Monster monster = World.world.getMonstre(monsterId);
        if (monster == null || monster.getGradeByLevel(lvl) == null) {
            return;
        }

        if (Config.getInstance().HALLOWEEN) {
            switch (monsterId) {
                case 98:
                    if (World.world.getMonstre(794) != null && World.world.getMonstre(794).getGradeByLevel(lvl) != null)
                        monsterId = 794;
                    break;
                case 101:
                    if (World.world.getMonstre(793) != null && World.world.getMonstre(793).getGradeByLevel(lvl) != null)
                        monsterId = 793;
                    break;
            }
        }

        Monster finalMonster = World.world.getMonstre(monsterId);
        if (finalMonster != null && finalMonster.getGradeByLevel(lvl) != null) {
            this.mobPossibles.add(finalMonster.getGradeByLevel(lvl));
        }
    }

    private int extractMaxTeam(String places) {
        if (places == null || places.isEmpty() || "|".equals(places)) {
            return 0;
        }
        int separatorIndex = places.indexOf('|');
        if (separatorIndex == -1 || separatorIndex + 1 >= places.length()) {
            return 0;
        }
        return (places.length() - separatorIndex - 1) / 2;
    }

    private int[] parseMapPos(String mapPos) {
        int firstComma = mapPos.indexOf(',');
        int secondComma = mapPos.indexOf(',', firstComma + 1);
        return new int[] {
                Integer.parseInt(mapPos.substring(0, firstComma)),
                Integer.parseInt(mapPos.substring(firstComma + 1, secondComma)),
                Integer.parseInt(mapPos.substring(secondComma + 1))
        };
    }

    private void applyForbidden(String forbidden) {
        int index = 0;
        int flag = 0;
        while (flag < 7) {
            int next = forbidden.indexOf(';', index);
            if (next == -1) {
                next = forbidden.length();
            }
            boolean enabled = next > index && forbidden.charAt(index) == '1';
            switch (flag) {
                case 0: noMarchand = enabled; break;
                case 1: noCollector = enabled; break;
                case 2: noPrism = enabled; break;
                case 3: noTP = enabled; break;
                case 4: noDefie = enabled; break;
                case 5: noAgro = enabled; break;
                case 6: noCanal = enabled; break;
            }
            index = next + 1;
            flag++;
            if (next >= forbidden.length()) {
                break;
            }
        }
    }

    private void rebuildCaseIndex() {
        int maxCaseId = -1;
        for (GameCase gameCase : this.cases) {
            if (gameCase != null && gameCase.getId() > maxCaseId) {
                maxCaseId = gameCase.getId();
            }
        }
        this.casesById = maxCaseId >= 0 ? new GameCase[maxCaseId + 1] : new GameCase[0];
        for (GameCase gameCase : this.cases) {
            if (gameCase != null) {
                this.casesById[gameCase.getId()] = gameCase;
            }
        }
    }


    public Fight newFight(Player init1, Player init2, int type) {
        if (init1.getFight() != null || init2.getFight() != null)
            return null;
        int id = 1;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Fight) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        Fight f = new Fight(type, id, this, init1, init2);
        this.fights.add(f);
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
        return f;
    }

    public void removeFight(int id) {
        if(this.fights != null) {
            Iterator<Fight> iterator = this.getFights().iterator();
            while(iterator.hasNext()) {
                Fight fight = iterator.next();
                if(fight != null && fight.getId() == id) {
                    iterator.remove();
                    break;
                }
            }

            if(this.fights.isEmpty()) this.fights = null;
        }
    }

    public int getNbrFight() {
        return fights == null ? 0 : this.fights.size();
    }

    public Fight getFight(int id) {
        if(this.fights != null)
            for(Fight fight : this.fights)
                if(fight.getId() == id)
                    return fight;
        return null;
    }

    public List<Fight> getFights() {
        if(this.fights == null)
            return new ArrayList<>();
        return fights;
    }

    public Map<Integer, Monster.MobGroup> getMobGroups() {
        return this.mobGroups;
    }

    public void removeNpcOrMobGroup(int id) {
        this.npcs.remove(id);
        this.mobGroups.remove(id);
    }

    public Npc addNpc(int npcID, int cellID, int dir) {
        NpcTemplate temp = World.world.getNPCTemplate(npcID);
        if (temp == null)
            return null;
        if (getCase(cellID) == null)
            return null;
        Npc npc;
        if(temp.getPath().isEmpty())
            npc = new Npc(this.nextObjectId, cellID, (byte) dir, temp);
        else
            npc = new NpcMovable(this.nextObjectId, cellID, (byte) dir, this.id, temp);
        this.npcs.put(this.nextObjectId, npc);
        this.nextObjectId--;
        return npc;
    }

    public Map<Integer, Npc> getNpcs() {
        return this.npcs;
    }

    public Npc getNpc(int id) {
        return this.npcs.get(id);
    }

    public Npc RemoveNpc(int id) {
        return this.npcs.remove(id);
    }

    public void applyEndFightAction(Player player) {
        if (this.endFightAction.get(player.needEndFight()) == null)
            return;
        if (this.id ==  8545) {
            if (player.getCurCell().getId() <= 193 && player.getCurCell().getId() != 186 && player.getCurCell().getId() != 187 && player.getCurCell().getId() != 173 && player.getCurCell().getId() != 172 && player.getCurCell().getId() != 144 && player.getCurCell().getId() != 158) {
                for (Action A : this.endFightAction.get(player.needEndFight())) {
                    A.apply(player, null, -1, -1);
                }
            } else {
                for (Action A : this.endFightAction.get(player.needEndFight())) {
                    A.setArgs("8547,214");
                    A.apply(player, null, -1, -1);
                }
            }
        } else {
            for (Action A : this.endFightAction.get(player.needEndFight()))
                A.apply(player, null, -1, -1);
        }
        player.setNeededEndFight(-1, null);
    }

    public boolean hasEndFightAction(int type) {
        return this.endFightAction.get(type) != null;
    }

    public void addEndFightAction(int type, Action A) {
        if (this.endFightAction.get(type) == null)
            this.endFightAction.put(type, new ArrayList<>()); // On retire l'action si elle existait déjà
        delEndFightAction(type, A.getId());
        this.endFightAction.get(type).add(A);
    }

    public void delEndFightAction(int type, int aType) {
        if (this.endFightAction.get(type) != null)
            new ArrayList<>(this.endFightAction.get(type)).stream().filter(A -> A.getId() == aType).forEach(A -> this.endFightAction.get(type).remove(A));
    }

    public void delAllEndFightAction() {
        this.endFightAction.clear();
    }

    public int getX() {
        return this.X;
    }

    public int getY() {
        return this.Y;
    }

    public SubArea getSubArea() {
        return this.subArea;
    }

    public MountPark getMountPark() {
        return this.mountPark;
    }

    public void setMountPark(MountPark mountPark) {
        this.mountPark = mountPark;
    }

    public int getMaxGroupNumb() {
        return this.maxGroup;
    }

    public int getMaxTeam() {
        return this.maxTeam;
    }

    public boolean containsForbiddenCellSpawn(int id) {
        if(this.mountPark != null)
            if(this.mountPark.getCellAndObject().containsKey(id))
                return true;
        return false;
    }

    public GameMap getMapCopy() {
        List<GameCase> cases = new ArrayList<>();

        GameMap map = new GameMap(id, date, w, h, key, placesStr);

        for (GameCase gameCase : this.cases) {
            if (map.getId() == 8279) {
                switch (gameCase.getId()) {
                    case 187:
                    case 170:
                    case 156:
                    case 142:
                    case 128:
                    case 114:
                    case 100:
                    case 86:
                        continue;
                }
            }

            //cases.add(new GameCase(map, gameCase.getId(), gameCase.isWalkable(true, true, -1), gameCase.isLoS(), (gameCase.getObject() == null ? -1 : gameCase.getObject().getId())));
            cases.add(new GameCase(map,gameCase.getId(),gameCase.getActivo(),gameCase.getMovimiento(),gameCase.getLevel(),gameCase.getSlope(),gameCase.isWalkable(true,true,-1),gameCase.isLoS(),(gameCase.getObject()==null ? -1 : gameCase.getObject().getId())));
        }
        map.setCases(cases);
        return map;
    }

    public GameMap getMapCopyIdentic() {
        GameMap map = new GameMap(id, date, w, h, key, placesStr, X, Y, maxGroup, fixSize, minSize, maxSize);
        List<GameCase> cases = this.cases.stream().map(entry -> new GameCase(map,entry.getId(),entry.getActivo(),entry.getMovimiento(),entry.getLevel(),entry.getSlope(),entry.isWalkable(false),entry.isLoS(),(entry.getObject()==null ? -1 : entry.getObject().getId()))).collect(Collectors.toList());
        map.setCases(cases);
        return map;
    }

    public void addPlayer(Player perso) {
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this, perso);
        perso.getCurCell().addPlayer(perso);
        if (perso.getEnergy() > 0) {
            if (perso.getEnergy() >= 10000)
                return;
            if (Constant.isTaverne(this) && perso.getTimeTaverne() == 0) {
                perso.setTimeTaverne(System.currentTimeMillis());
            } else if (perso.getTimeTaverne() != 0) {
                int gain = (int) ((System.currentTimeMillis() - perso.getTimeTaverne()) / 1000);
                if(gain >= 10000) gain = 10000 - perso.getEnergy();
                perso.setEnergy(perso.getEnergy() + gain);
                if (perso.getEnergy() >= 10000) perso.setEnergy(10000);
                SocketManager.GAME_SEND_Im_PACKET(perso, "092;" + gain);
                SocketManager.GAME_SEND_STATS_PACKET(perso);
                perso.setTimeTaverne(0);
            }
        }
    }

    public ArrayList<Player> getPlayers() {
        ArrayList<Player> player = new ArrayList<>();
        for (GameCase c : cases)
            player.addAll(c.getPlayers().stream().collect(Collectors.toList()));
        return player;
    }

    public void sendFloorItems(Player perso) {
        this.cases.stream().filter(c -> c.getDroppedItem(false) != null).forEach(c -> SocketManager.GAME_SEND_GDO_PACKET(perso, '+', c.getId(), c.getDroppedItem(false).getTemplate().getId(), 0));
    }

    public void delAllDropItem() {
        for (GameCase gameCase : this.cases) {
            SocketManager.GAME_SEND_GDO_PACKET_TO_MAP(this, '-', gameCase.getId(), 0, 0);
            gameCase.clearDroppedItem();
        }
    }

    public int getStoreCount() {
        if (World.world.getSeller(this.getId()) == null)
            return 0;
        return World.world.getSeller(this.getId()).size();
    }

    public boolean haveMobFix() {
        return this.fixMobGroups.size() > 0;
    }

    public boolean isPossibleToPutMonster() {
        return !this.cases.isEmpty() && this.maxGroup > 0 && this.mobPossibles.size() > 0;
    }

    public boolean loadExtraMonsterOnMap(int idMob) {
        if (World.world.getMonstre(idMob) == null)
            return false;
        Monster.MobGrade grade = World.world.getMonstre(idMob).getRandomGrade();
        int cell = this.getRandomFreeCellId();

        Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, Constant.ALIGNEMENT_NEUTRE, this.mobPossibles, this, cell, this.fixSize, this.maxSize, this.maxSize, grade);
        if (group.getMobs().isEmpty())
            return false;
        group.setStarBonus(0);
        this.mobGroups.put(this.nextObjectId, group);
        this.nextObjectId--;
        return true;
    }

    public void loadMonsterOnMap() {
        if (maxGroup == 0)
            return;
        spawnGroup(Constant.ALIGNEMENT_NEUTRE, this.maxGroup, false, -1);//Spawn des groupes d'alignement neutre
        spawnGroup(Constant.ALIGNEMENT_BONTARIEN, 1, false, -1);//Spawn du groupe de gardes bontarien s'il y a
        spawnGroup(Constant.ALIGNEMENT_BRAKMARIEN, 1, false, -1);//Spawn du groupe de gardes brakmarien s'il y a
        
        // Restaurer les étoiles sauvegardées des groupes (persistence au redémarrage)
        restoreGroupsStars();
    }

    public void mute() {
        this.isMute = !this.isMute;
    }

    public boolean isMute() {
        return this.isMute;
    }

    public boolean isAggroByMob(Player perso, int cell) {
        if (placesStr.equalsIgnoreCase("|"))
            return false;
        if (perso.getCurMap().getId() != id || !perso.canAggro())
            return false;
        for (Monster.MobGroup group : this.mobGroups.values()) {
            if (perso.get_align() == 0 && group.getAlignement() > 0)
                continue;
            if (perso.get_align() == 1 && group.getAlignement() == 1)
                continue;
            if (perso.get_align() == 2 && group.getAlignement() == 2)
                continue;

            if (this.subArea != null) {
                group.setSubArea(this.subArea.getId());
                group.changeAgro();
            }
            if (PathFinding.getDistanceBetween(this, cell, group.getCellId()) <= group.getAggroDistance()
                    && group.getAggroDistance() > 0)//S'il y aggro
            {
                if (ConditionParser.validConditions(perso, group.getCondition()))
                    return true;
            }
        }
        return false;
    }

    public void spawnAfterTimeGroup() {
        ((ArrayList<RespawnGroup>) updatable.get()).add(new RespawnGroup(this, -1, System.currentTimeMillis(), 0));
    }

    public void spawnAfterTimeGroupFix(final int cell) {
        //((ArrayList<RespawnGroup>) updatable.get()).add(new RespawnGroup(this, cell, System.currentTimeMillis()));
    }

    public void setCellCache(CellCacheImpl cache) {
        this.cellCache = cache;
    }

    public Monster.MobGroup spawnGroupGladiatrool(String groupData) {
        while(this.mobGroups.get(this.nextObjectId) != null)
            this.nextObjectId--;
        int cell = this.getRandomFreeCellId();
        while (this.containsForbiddenCellSpawn(cell))
            cell = this.getRandomFreeCellId();
        Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, this, cell, groupData);
        if (group.getMobs().isEmpty())
            return group;
        this.mobGroups.put(this.nextObjectId, group);
        group.setIsFix(false);
        SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);

        this.nextObjectId--;
        return group;
    }


    private static class RespawnGroup {

        private final GameMap map;
        private final int cell;
        private final long lastTime;
        private final int stars; // Nombre d'étoiles du groupe à respawn

        public RespawnGroup(GameMap map, int cell, long lastTime, int stars) {
            this.map = map;
            this.cell = cell;
            this.lastTime = lastTime;
            this.stars = stars;
        }
    }

    /**
     * Calcule le délai de respawn (en millisecondes) en fonction du nombre d'étoiles
     * Les étoiles augmentent avec le temps entre les respawns
     * 
     * Sans étoiles (0): 2-5 minutes
     * 15 étoiles: 5-8 minutes
     * 30 étoiles: 8-11 minutes
     * 45 étoiles: 11-14 minutes
     * 60 étoiles: 14-17 minutes
     * 75+ étoiles: 17-20 minutes
     */
    private static long calculateDelayForStars(int stars) {
        int minDelay, maxDelay;
        
        if (stars <= 0) {
            // Sans étoiles: délai court (2-5 minutes)
            minDelay = 120000;
            maxDelay = 300000;
        } else if (stars < 30) {
            // 15 étoiles: 5-8 minutes
            minDelay = 300000;
            maxDelay = 480000;
        } else if (stars < 45) {
            // 30 étoiles: 8-11 minutes
            minDelay = 480000;
            maxDelay = 660000;
        } else if (stars < 60) {
            // 45 étoiles: 11-14 minutes
            minDelay = 660000;
            maxDelay = 840000;
        } else if (stars < 75) {
            // 60 étoiles: 14-17 minutes
            minDelay = 840000;
            maxDelay = 1020000;
        } else {
            // 75+ étoiles: 17-20 minutes
            minDelay = 1020000;
            maxDelay = 1200000;
        }
        
        return Formulas.getRandomValue(minDelay, maxDelay);
    }

    public void spawnGroup(int align, int nbr, boolean log, int cellID) {
        if (nbr < 1)
            return;
        if (this.mobGroups.size() - this.fixMobGroups.size() >= this.maxGroup)
            return;
        for (int a = 1; a <= nbr; a++) {
            // mobExtras
            ArrayList<Monster.MobGrade> mobPoss = new ArrayList<>(this.mobPossibles);
            if (!this.mobExtras.isEmpty()) {
                for (Entry<Integer, Integer> entry : this.mobExtras.entrySet()) {
                    if (entry.getKey() == 499) // Si c'est un minotoboule de nowel
                        if (!Config.getInstance().NOEL) // Si ce n'est pas nowel
                            continue;
                    int random = Formulas.getRandomValue(0, 99);
                    while (entry.getValue() > random) {
                        Monster mob = World.world.getMonstre(entry.getKey());
                        if (mob == null)
                            continue;
                        Monster.MobGrade mobG = mob.getRandomGrade();
                        if (mobG == null)
                            continue;
                        mobPoss.add(mobG);
                        if (entry.getKey() == 422 || entry.getKey() == 499) // un seul DDV / Minotoboule
                            break;
                        random = Formulas.getRandomValue(0, 99);
                    }
                }
            }

            while(this.mobGroups.get(this.nextObjectId) != null)
                this.nextObjectId--;

            Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, align, mobPoss, this, cellID, this.fixSize, this.minSize, this.maxSize, null);

            if (group.getMobs().isEmpty())
                continue;
            
            // Comportement classique : un groupe qui spawn repart a 0 etoile.
            group.setStarBonus(0);
            
            this.mobGroups.put(this.nextObjectId, group);
            if (log)
                SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);

            // DEBUG : info serveur sur le spawn du groupe
            this.send("cs<font color='#FF69B4'>[DEBUG] Spawn du groupe " + this.nextObjectId +
                    " align=" + align +
                    " sur la map " + this.id +
                    " cell=" + group.getCellId() + "</font>");

            this.nextObjectId--;
        }
    }

    public void respawnGroup(Monster.MobGroup group) {
        this.mobGroups.put(group.getId(), group);
        SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
    }

    public void spawnGroupWith(Monster m) {
        while(this.mobGroups.get(this.nextObjectId) != null)
            this.nextObjectId--;
        Monster.MobGrade _m = null;
        while (_m == null)
            _m = m.getRandomGrade();
        int cell = this.getRandomFreeCellId();
        while (this.containsForbiddenCellSpawn(cell))
            cell = this.getRandomFreeCellId();

        Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, -1, this.mobPossibles, this, cell, this.fixSize, this.minSize, this.maxSize, _m);
        group.setIsFix(false);
        group.setStarBonus(0);
        this.mobGroups.put(this.nextObjectId, group);
        SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
        this.nextObjectId--;
    }

    public void spawnNewGroup(boolean timer, int cellID, String groupData, String condition) {
        while(this.mobGroups.get(this.nextObjectId) != null)
            this.nextObjectId--;
        while (this.containsForbiddenCellSpawn(cellID))
            cellID = this.getRandomFreeCellId();

        Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, cellID, groupData);
        if (group.getMobs().isEmpty())
            return;
        group.setStarBonus(0);
        this.mobGroups.put(this.nextObjectId, group);
        group.setCondition(condition);
        group.setIsFix(false);
        SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
        this.nextObjectId--;
        if (timer)
            group.startCondTimer();
    }

    public boolean spawnGroupOnCommand(int cellID, String groupData, boolean send) {
        if (groupData == null || groupData.trim().isEmpty())
            return false;

        while(this.mobGroups.get(this.nextObjectId) != null)
            this.nextObjectId--;

        while (this.containsForbiddenCellSpawn(cellID))
            cellID = this.getRandomFreeCellId();

        Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, cellID, groupData);
        if (group.getMobs().isEmpty())
            return false;
        group.setStarBonus(0);
        this.mobGroups.put(this.nextObjectId, group);
        group.setIsFix(false);
        if (send)
            SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);

        this.nextObjectId--;
        return true;
    }

    public void addStaticGroup(int cellID, String groupData, boolean b) {
        while(this.mobGroups.get(this.nextObjectId) != null)
            this.nextObjectId--;
        Monster.MobGroup group = new Monster.MobGroup(this.nextObjectId, cellID, groupData);
        if (group.getMobs().isEmpty())
            return;
        group.setStarBonus(0);
        this.mobGroups.put(this.nextObjectId, group);
        this.nextObjectId--;
        this.fixMobGroups.put(-1000 + this.nextObjectId, group);
        if (b)
            SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
    }

    public void refreshSpawns() {
        for (int id : this.mobGroups.keySet()) {
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this, id);
        }
        this.mobGroups.clear();
        this.mobGroups.putAll(this.fixMobGroups);
        for (Monster.MobGroup mg : this.fixMobGroups.values())
            SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, mg);

        spawnGroup(Constant.ALIGNEMENT_NEUTRE, this.maxGroup, true, -1);//Spawn des groupes d'alignement neutre
        spawnGroup(Constant.ALIGNEMENT_BONTARIEN, 1, true, -1);//Spawn du groupe de gardes bontarien s'il y a
        spawnGroup(Constant.ALIGNEMENT_BRAKMARIEN, 1, true, -1);//Spawn du groupe de gardes brakmarien s'il y a
    }

    public String getGMsPackets() {
        StringBuilder packet = new StringBuilder();
        cases.stream().filter(cell -> cell != null).forEach(cell -> cell.getPlayers().stream().filter(player ->
                player != null).forEach(player -> packet.append("GM|+").append(player.parseToGM()).append('\u0000')));
        return packet.toString();
    }

    public String getFightersGMsPackets(Fight fight) {
        StringBuilder packet = new StringBuilder("GM");
        for (GameCase cell : this.cases)
            cell.getFighters().stream().filter(fighter -> fighter.getFight() == fight)
                    .forEach(fighter -> packet.append("|").append(fighter.getGmPacket('+', false)));
        return packet.toString();
    }

    public String getFighterGMPacket(Player player) {
        Fighter target = player.getFight().getFighterByPerso(player);
        for (GameCase cell : this.cases)
            for(Fighter fighter : cell.getFighters())
                if(fighter.getFight() == player.getFight() && fighter == target)
                    return "GM|" + fighter.getGmPacket('~', false);
        return "";
    }

    public String getMobGroupGMsPackets() {
        if (this.mobGroups.isEmpty())
            return "";

        StringBuilder packet = new StringBuilder();
        packet.append("GM|");
        boolean isFirst = true;
        for (Monster.MobGroup entry : this.mobGroups.values()) {
            String GM = entry.parseGM();
            if (GM.equals(""))
                continue;

            if (!isFirst)
                packet.append("|");

            packet.append(GM);
            isFirst = false;
        }
        return packet.toString();
    }

    public String getPrismeGMPacket() {
        String str = "";
        Collection<Prism> prisms = World.world.AllPrisme();
        if (prisms != null) {
            for (Prism prism : prisms) {
                if (prism.getMap() == this.id) {
                    str = prism.getGMPrisme();
                    break;
                }
            }
        }
        return str;
    }

    public String getNpcsGMsPackets(Player p) {
        if (this.npcs.isEmpty())
            return "";

        StringBuilder packet = new StringBuilder();
        packet.append("GM|");
        boolean isFirst = true;
        for (Entry<Integer, Npc> entry : this.npcs.entrySet()) {
            String GM = entry.getValue().parse(false, p);
            if (GM.equals(""))
                continue;

            if (!isFirst)
                packet.append("|");

            packet.append(GM);
            isFirst = false;
        }
        return packet.toString();
    }

    public String getObjectsGDsPackets() {
        StringBuilder packet = new StringBuilder("GDF");
        this.cases.stream().filter(gameCase -> gameCase.getObject() != null)
                .forEach(gameCase -> packet.append("|").append(gameCase.getId()).append(";").append(gameCase.getObject().getState())
                        .append(";").append((gameCase.getObject().isInteractive() ? "1" : "0")));
        return packet.toString();
    }


    public void startFightVersusMonstres(Player perso, Monster.MobGroup group) {
        if (perso.getFight() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        if (this.placesStr.isEmpty() || this.placesStr.equals("|")) {
            perso.sendMessage("Poste sur le forum dans la catégorie adéquat avec l'id de la map (/mapid dans le tchat) afin de pouvoir y mettre les cellules de combat. Merci.");
            return;
        }
        if (Main.fightAsBlocked || perso.isDead() == 1 || perso.afterFight || !perso.canAggro())
            return;
        if (perso.get_align() == 0 && group.getAlignement() > 0)
            return;
        if (perso.get_align() == 1 && group.getAlignement() == 1)
            return;
        if (perso.get_align() == 2 && group.getAlignement() == 2)
            return;
        if (!perso.canAggro())
            return;
        if (perso.afterFight)
            return;
        if (!group.getCondition().equals(""))
            if (!ConditionParser.validConditions(perso, group.getCondition())) {
                SocketManager.GAME_SEND_Im_PACKET(perso, "119");
                return;
            }

        final Party party = perso.getParty();
        if (party != null && party.getMaster() != null
                && !party.getMaster().getName().equals(perso.getName())
                && party.isWithTheMaster(perso, false))
            return;

        int id = getFightID();

        // DEBUG : joueur lance un combat
        this.send("cs<font color='#FF69B4'>[DEBUG] Joueur " + perso.getName() +
                " lance un combat contre le groupe " + group.getId() +
                " sur la map " + this.id +
                " (cell " + group.getCellId() + ") - " + group.getStarBonus() + " étoiles</font>");

        // On enlève le groupe engagé de la map
        this.mobGroups.remove(group.getId());
        this.savedGroupsStars.remove(group.getCellId());

        // === Respawn immédiat en respectant maxGroup ===

        if (group.isFix()) {
            // DONJONS: Respawn instantané du groupe fixe
            int cell = group.getCellId();
            Map<String, String> data = World.world.getGroupFix(this.id, cell);

            if (data != null) {
                String groupData = data.get("groupData");
                if (groupData != null && !groupData.isEmpty()) {
                    this.addStaticGroup(cell, groupData, true);
                }
            }
        } else {
            // MAPS CLASSIQUES: Respawn les groupes manquants (sans étoiles)
            int currentNonFixGroups = this.mobGroups.size() - this.fixMobGroups.size();
            int spotsToFill = this.maxGroup - currentNonFixGroups;
            
            if (spotsToFill > 0) {
                // Respawn autant de groupes que nécessaire pour remplir la map
                spawnGroup(Constant.ALIGNEMENT_NEUTRE, spotsToFill, true, -1);
                this.send("cs<font color='#FF69B4'>[DEBUG] Respawn de " + spotsToFill + 
                        " groupe(s) sur la map " + this.id + "</font>");
            } else {
                this.send("cs<font color='#FF69B4'>[DEBUG] Pas de respawn: " + currentNonFixGroups + 
                        "/" + this.maxGroup + " groupes</font>");
            }
        }

        // === Création du combat ===
        perso.setOldMap(perso.getCurMap().getId());
        perso.setOldCell(perso.getCurCell().getId());
        Fight f = new Fight(id, this, perso, group);
        this.fights.add(f);
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);

        //.maitre (v1) car la partie du code de party bug
        if (perso.get_maitre() != null) {
            for (Player p : perso.get_maitre().getEsclaves()) {
                if (p.getFight() != null) continue;
                if (p.getCurMap().getId() != perso.getCurMap().getId()) continue;
                SocketManager.GAME_SEND_MESSAGE(perso, "Vos joueurs rentreront automatiquement en combat dans 1 seconde.");
                f.joinFight(p, perso.getId());
                p.getAccount().getGameClient().readyFight("GR1");
            }
        }
        if (perso.getTacticalMode())
            SocketManager.sendTacticalTruePacket(perso.getGameClient());
    }

    public static boolean IsInDj (GameMap map) {

        Map<Integer, ArrayList<Action>> actionsOnMap = map.endFightAction;
        for (Map.Entry<Integer, ArrayList<Action> > entry : actionsOnMap.entrySet()) {
            ArrayList<Action> Actions =  entry.getValue();
            for (Action action : Actions) {
                //player.sendMessage("L'action " + action.getId() + " " + action.getArgs());
                if( action.getId() == 0 ){
                    return true;
                }
            }
        }
        return false;
    }

    //.maitre (v1)
        /*
		if(perso.get_maitre()!=null)
		{
			for(Player p: perso.get_maitre().getEsclaves())
			{
				if(p.getFight()!=null)continue;
				if(p.getCurMap().getId() != perso.getCurMap().getId()) continue;
    		    SocketManager.GAME_SEND_MESSAGE(perso, "Vos joueurs rentreront automatiquement en combat dans 1 seconde.");
    			f.joinFight(p, perso.getId());
    			p.getAccount().getGameClient().readyFight("GR1");
			}
		}*/

    //.master (v2)
		/*
		final Party party = perso.getParty();


        if(party != null && party.getMaster() != null && !party.getMaster().getName().equals(perso.getName()) && party.isWithTheMaster(perso, false)) return;

        //int id = 1;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Fight) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;


        this.mobGroups.remove(group.getId());
        Fight fight = new Fight(id, this, perso, group);
        this.fights.add(fight);
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);


        if(party != null && party.getMaster() != null && party.getMaster().getName().equals(perso.getName())) {
            TimerWaiter.addNext(() -> party.getPlayers().stream().filter((follower) -> party.isWithTheMaster(follower, false)).forEach(follower -> {
                if (fight.getPrism() != null)
                    fight.joinPrismFight(follower, (fight.getTeam0().containsKey(perso.getId()) ? 0 : 1));
                else
                    fight.joinFight(follower, perso.getId());
                	perso.getAccount().getGameClient().readyFight("GR1");
            }), 1, TimeUnit.SECONDS, TimerWaiter.DataType.CLIENT);
        }
        */


    //v2.8 - New Fight ID
    public synchronized short getFightID()
    {
        if(fights==null)
            fights=new ArrayList<>();
        if(fights.isEmpty())
            return 1;
        for(short id=1;true;id++)
        {
            if(fights.size()<id)
                return id;
            if(fights.get(id-1)==null)
                return id;
        }
    }

    //v2.7 - Tactical mode memory
    public void startFightVersusProtectors(Player perso, Monster.MobGroup group) {
        if (perso.getFight() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        if (Main.fightAsBlocked)
            return;
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
        int id = 1;

        if (this.placesStr.isEmpty() || this.placesStr.equals("|")) {
            perso.sendMessage("Poste sur le forum dans la catégorie adéquat avec l'id de la map (/mapid dans le tchat) afin de pouvoir y mettre les cellules de combat. Merci.");
            return;
        }
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Fight) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        this.fights.add(new Fight(id, this, perso, group, Constant.FIGHT_TYPE_PVM));
        if(perso.getTacticalMode())
            SocketManager.sendTacticalTruePacket(perso.getGameClient());
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    //v2.7 - Tactical mode memory
    public void startFigthVersusDopeuls(Player perso, Monster.MobGroup group)//RaZoR
    {
        if (perso.getFight()!=null||Main.fightAsBlocked||perso.isDead()==1||!perso.canAggro()||perso.afterFight)
            return;
        if (perso.isInAreaNotSubscribe()) {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        int id = 1;
        /*
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
            */
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Fight) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        this.fights.add(new Fight(id, this, perso, group, Constant.FIGHT_TYPE_DOPEUL));
        if(perso.getTacticalMode())
            SocketManager.sendTacticalTruePacket(perso.getGameClient());
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    //v2.7 - Tactical mode memory
    public void startFightVersusPercepteur(Player perso, Collector perco) {
        if (perso.getFight() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        if (Main.fightAsBlocked)
            return;
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
        int id = 1;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Fight) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;

        this.fights.add(new Fight(id, this, perso, perco));
        if(perso.getTacticalMode())
            SocketManager.sendTacticalTruePacket(perso.getGameClient());
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    //v2.7 - Tactical mode memory
    public void startFightVersusPrisme(Player perso, Prism Prisme) {
        if (perso.getFight() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        if (Main.fightAsBlocked)
            return;
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
        int id = 1;
        if (!this.fights.isEmpty())
            id = ((Fight) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        this.fights.add(new Fight(id, this, perso, Prisme));
        if(perso.getTacticalMode())
            SocketManager.sendTacticalTruePacket(perso.getGameClient());
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    public int getRandomFreeCellId() {
        ArrayList<Integer> freecell = new ArrayList<>();

        for (GameCase entry : cases) {
            if (entry == null)
                continue;
            if (!entry.isWalkable(true))
                continue;
            if (entry.getObject() != null)
                continue;
            if(this.id == 8279) {
                switch(entry.getId()) {
                    case 86:
                    case 100:
                    case 114:
                    case 128:
                    case 142:
                    case 156:
                    case 170:
                    case 184:
                    case 198:
                        continue;
                }
            }
            if (this.mountPark != null)
                if (this.mountPark.getCellOfObject().contains((int) entry.getId()))
                    continue;

            boolean ok = true;
            if (this.mobGroups != null)
                for (Monster.MobGroup mg : this.mobGroups.values())
                    if (mg != null)
                        if (mg.getCellId() == entry.getId())
                            ok = false;
            if (this.npcs != null)
                for (Npc npc : this.npcs.values())
                    if (npc != null)
                        if (npc.getCellid() == entry.getId())
                            ok = false;

            if (!ok || !entry.getPlayers().isEmpty())
                continue;
            freecell.add(entry.getId());
        }

        if (freecell.isEmpty())
            return -1;
        return freecell.get(Formulas.getRandomValue(0, freecell.size() - 1));
    }

    public int getRandomNearFreeCellId(int cellid)//obtenir une cell aléatoire et proche
    {
        ArrayList<Integer> freecell = new ArrayList<>();
        ArrayList<Integer> cases = new ArrayList<>();

        cases.add((cellid + 1));
        cases.add((cellid - 1));
        cases.add((cellid + 2));
        cases.add((cellid - 2));
        cases.add((cellid + 14));
        cases.add((cellid - 14));
        cases.add((cellid + 15));
        cases.add((cellid - 15));
        cases.add((cellid + 16));
        cases.add((cellid - 16));
        cases.add((cellid + 27));
        cases.add((cellid - 27));
        cases.add((cellid + 28));
        cases.add((cellid - 28));
        cases.add((cellid + 29));
        cases.add((cellid - 29));
        cases.add((cellid + 30));
        cases.add((cellid - 30));
        cases.add((cellid + 31));
        cases.add((cellid - 31));
        cases.add((cellid + 42));
        cases.add((cellid - 42));
        cases.add((cellid + 43));
        cases.add((cellid - 43));
        cases.add((cellid + 44));
        cases.add((cellid - 44));
        cases.add((cellid + 45));
        cases.add((cellid - 45));
        cases.add((cellid + 57));
        cases.add((cellid - 57));
        cases.add((cellid + 58));
        cases.add((cellid - 58));
        cases.add((cellid + 59));
        cases.add((cellid - 59));

        for (int entry : cases) {
            GameCase gameCase = this.getCase(entry);
            if (gameCase == null)
                continue;
            if(gameCase.getOnCellStopAction())
                continue;
            //Si la case n'est pas marchable
            if (!gameCase.isWalkable(true))
                continue;
            //Si la case est prise par un groupe de monstre
            boolean ok = true;
            for (Entry<Integer, Monster.MobGroup> mgEntry : this.mobGroups.entrySet())
                if (mgEntry.getValue().getCellId() == gameCase.getId())
                    ok = false;
            if (!ok)
                continue;
            //Si la case est prise par un npc
            ok = true;
            for (Entry<Integer, Npc> npcEntry : this.npcs.entrySet())
                if (npcEntry.getValue().getCellid() == gameCase.getId())
                    ok = false;
            if (!ok)
                continue;
            //Si la case est prise par un joueur
            if (!gameCase.getPlayers().isEmpty())
                continue;
            //Sinon
            freecell.add(gameCase.getId());
        }
        if (freecell.isEmpty())
            return -1;
        int rand = Formulas.getRandomValue(0, freecell.size() - 1);
        return freecell.get(rand);
    }

    public void onMapMonsterDeplacement() {
        this.onMapMonsterDeplacement(false);
    }

    public void forceMapMonsterDeplacement() {
        this.onMapMonsterDeplacement(true);
    }

    private void onMapMonsterDeplacement(boolean force) {
        if (!force && !Config.getInstance().mobGroupMovement)
            return;
        if (getMobGroups().size() == 0)
            return;
        int RandNumb = Formulas.getRandomValue(1, getMobGroups().size());
        int i = 0;
        for (Monster.MobGroup group : getMobGroups().values()) {
            switch (this.id) {
                case 8279:// W:15   H:17
                    final int cell1 = group.getCellId();
                    final GameCase cell2 = this.getCase((cell1 - 15)), cell3 = this.getCase((cell1 - 15 + 1));
                    final GameCase cell4 = this.getCase((cell1 + 15 - 1)),
                            cell5 = this.getCase((cell1 + 15));
                    boolean case2 = (cell2 != null && (cell2.isWalkable(true) && (cell2.getPlayers().isEmpty())));
                    boolean case3 = (cell3 != null && (cell3.isWalkable(true) && (cell3.getPlayers().isEmpty())));
                    boolean case4 = (cell4 != null && (cell4.isWalkable(true) && (cell4.getPlayers().isEmpty())));
                    boolean case5 = (cell5 != null && (cell5.isWalkable(true) && (cell5.getPlayers().isEmpty())));
                    ArrayList<Boolean> array = new ArrayList<>();
                    array.add(case2);
                    array.add(case3);
                    array.add(case4);
                    array.add(case5);

                    int count = 0;
                    for (boolean bo : array)
                        if (bo)
                            count++;

                    if (count == 0)
                        return;
                    if (count == 1) {
                        GameCase newCell = (case2 ? cell2 : (case3 ? cell3 : (case4 ? cell4 : cell5)));
                        GameCase nextCell = null;
                        if (newCell == null)
                            return;

                        if (newCell.equals(cell2)) {
                            if (checkCell(newCell.getId() - 15)) {
                                nextCell = this.getCase(newCell.getId() - 15);
                                if (this.checkCell(nextCell.getId() - 15)) {
                                    nextCell = this.getCase(nextCell.getId() - 15);
                                }
                            }
                        } else if (newCell.equals(cell3)) {
                            if (this.checkCell(newCell.getId() - 15 + 1)) {
                                nextCell = this.getCase(newCell.getId() - 15 + 1);
                                if (this.getCase(nextCell.getId() - 15 + 1) != null) {
                                    nextCell = this.getCase(nextCell.getId() - 15 + 1);
                                }
                            }
                        } else if (newCell.equals(cell4)) {
                            if (this.checkCell(newCell.getId() + 15 - 1)) {
                                nextCell = this.getCase(newCell.getId() + 15 - 1);
                                if (this.checkCell(nextCell.getId() + 15 - 1)) {
                                    nextCell = this.getCase(nextCell.getId() + 15 - 1);
                                }
                            }
                        } else if (newCell.equals(cell5)) {
                            if (this.checkCell(newCell.getId() + 15)) {
                                nextCell = this.getCase(newCell.getId() + 15);
                                if (this.checkCell(nextCell.getId() + 15)) {
                                    nextCell = this.getCase(nextCell.getId() + 15);
                                }
                            }
                        }

                        String pathstr;
                        try {
                            pathstr = PathFinding.getShortestStringPathBetween(this, group.getCellId(), nextCell.getId(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        if (pathstr == null)
                            return;
                        group.setCellId(nextCell.getId());
                        for (Player z : getPlayers())
                            SocketManager.GAME_SEND_GA_PACKET(z.getGameClient(), "0", "1", group.getId()
                                    + "", pathstr);
                    } else {
                        if (group.isFix())
                            continue;
                        i++;
                        if (i != RandNumb)
                            continue;

                        int cell = -1;
                        while (cell == -1 || cell == 383 || cell == 384
                                || cell == 398 || cell == 369)
                            cell = getRandomNearFreeCellId(group.getCellId());
                        String pathstr;
                        try {
                            pathstr = PathFinding.getShortestStringPathBetween(this, group.getCellId(), cell, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        if (pathstr == null)
                            return;
                        group.setCellId(cell);
                        for (Player z : getPlayers())
                            SocketManager.GAME_SEND_GA_PACKET(z.getGameClient(), "0", "1", group.getId() + "", pathstr);
                    }
                    break;

                default:
                    if (group.isFix())
                        continue;
                    i++;
                    if (i != RandNumb)
                        continue;
                    int cell = getRandomNearFreeCellId(group.getCellId());
                    String pathstr;
                    try {
                        pathstr = PathFinding.getShortestStringPathBetween(this, group.getCellId(), cell, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (pathstr == null)
                        return;
                    group.setCellId(cell);
                    for (Player z : getPlayers())
                        SocketManager.GAME_SEND_GA_PACKET(z.getGameClient(), "0", "1", group.getId()
                                + "", pathstr);
                    break;
            }

        }
    }

    public boolean checkCell(int id) {
        return this.getCase(id - 15) != null && this.getCase(id - 15).isWalkable(true);
    }

    public String getObjects() {
        if (this.mountPark == null || this.mountPark.getObject().size() == 0)
            return "";
        String packets = "GDO+";
        boolean first = true;
        for (Entry<Integer, Map<Integer, Integer>> entry : this.mountPark.getObject().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (!first)
                    packets += "|";
                int cellidDurab = entry.getKey();
                packets += entry.getKey() + ";" + entry2.getKey() + ";1;"
                        + getObjDurable(cellidDurab);
                first = false;
            }
        }
        return packets;
    }

    public String getObjDurable(int CellID) {
        String packets = "";
        for (Entry<Integer, Map<Integer, Integer>> entry : this.mountPark.getObjDurab().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (CellID == entry.getKey())
                    packets += entry2.getValue() + ";" + entry2.getKey();
            }
        }
        return packets;
    }

    public boolean cellSideLeft(int cell) {
        int ladoIzq = this.w;
        for (int i = 0; i < this.w; i++) {
            if (cell == ladoIzq)
                return true;
            ladoIzq = ladoIzq + (this.w * 2) - 1;
        }
        return false;
    }

    public boolean cellSideRight(int cell) {
        int ladoDer = 2 * (this.w - 1);
        for (int i = 0; i < this.w; i++) {
            if (cell == ladoDer)
                return true;
            ladoDer = ladoDer + (this.w * 2) - 1;
        }
        return false;
    }

    public boolean cellSide(int cell1, int cell2) {
        if (cellSideLeft(cell1))
            if (cell2 == cell1 + (this.w - 1) || cell2 == cell1 - this.w)
                return true;
        if (cellSideRight(cell1))
            if (cell2 == cell1 + this.w || cell2 == cell1 - (this.w - 1))
                return true;
        return false;
    }

    public String getGMOfMount(boolean ok) {
        if(this.mountPark == null || this.mountPark.getListOfRaising().size() == 0)
            return "";

        ArrayList<Mount> mounts = new ArrayList<>();

        for(Integer id : this.mountPark.getListOfRaising()) {
            Mount mount = World.world.getMountById(id);

            if(mount != null)
                if(this.getPlayer(mount.getOwner()) != null || this.mountPark.getGuild() != null)
                    mounts.add(mount);
        }

        if (ok)
            for(Player target : this.getPlayers())
                SocketManager.GAME_SEND_GM_MOUNT(target.getGameClient(), this, false);

        return this.getGMOfMount(mounts);
    }

    public String getGMOfMount(ArrayList<Mount> mounts) {
        if(this.mountPark == null || this.mountPark.getListOfRaising().size() == 0)
            return "";
        StringBuilder packets = new StringBuilder();
        packets.append("GM|+");
        boolean first = true;
        for(Mount mount : mounts) {
            String GM = mount.parseToGM();
            if(GM == null || GM.equals(""))
                continue;
            if(!first)
                packets.append("|+");
            packets.append(GM);
            first = false;
        }

        return packets.toString();
    }

    public Player getPlayer(int id) {
        for(GameCase cell : cases)
            for(Player player : cell.getPlayers())
                if(player != null)
                    if(player.getId() == id)
                        return player;
        return null;
    }

    public void onPlayerArriveOnCell(Player player, int id) {
        GameCase cell = this.getCase(id);
        if (cell == null)
            return;
        synchronized (cell) {
            GameObject obj = cell.getDroppedItem(true);
            if (obj != null && !Main.mapAsBlocked) {
                if (Logging.USE_LOG)
                    Logging.getInstance().write("Object", "GetInOnTheFloor : " + player.getName() + " a ramassé [" + obj.getTemplate().getId() + "@" + obj.getGuid() + ";" + obj.getQuantity() + "]");
                if (player.addObjet(obj, true))
                    World.world.addGameObject(obj, true);
                SocketManager.GAME_SEND_GDO_PACKET_TO_MAP(this, '-', id, 0, 0);
                SocketManager.GAME_SEND_Ow_PACKET(player);
            }
            if (obj != null && Main.mapAsBlocked)
                SocketManager.GAME_SEND_MESSAGE(player, "L'Administrateur à bloqué temporairement la récolte des objets aux sols.");
        }
        InteractiveDoor.check(player, this);
        this.getCase(id).applyOnCellStopActions(player);
        if (this.placesStr.equalsIgnoreCase("|"))
            return;
        if (player.getCurMap().getId() != this.id || !player.canAggro())
            return;
        if(!Constant.isInGladiatorDonjon(Integer.parseInt(String.valueOf(id)))) {
            for (Monster.MobGroup group : this.mobGroups.values()) {
                if (PathFinding.getDistanceBetween(this, id, group.getCellId()) <= group.getAggroDistance()) {//S'il y aggr
                    startFightVersusMonstres(player, group);
                    return;
                }
            }
        }
    }

    public void send(String packet) {
        this.getPlayers().stream().filter(player -> player != null).forEach(player -> SocketManager.send(player, packet));
    }

    private void updateMobGroupsStars() {
        long now = System.currentTimeMillis();
        List<Monster.MobGroup> changedGroups = new ArrayList<>();
        boolean traceStars = shouldTraceStars();
        Map<Integer, Integer> oldStarsByGroupId = traceStars ? new HashMap<>() : null;
        Map<Integer, Long> oldUpdateAtByGroupId = traceStars ? new HashMap<>() : null;

        for (Monster.MobGroup group : this.mobGroups.values()) {
            if (group == null) {
                continue;
            }

            if (traceStars) {
                oldStarsByGroupId.put(group.getId(), group.getStarBonus());
                oldUpdateAtByGroupId.put(group.getId(), group.getLastStarBonusUpdateAt());
            }

            if (group.updateStarBonus(now)) {
                changedGroups.add(group);
            }
        }

        if (traceStars && !changedGroups.isEmpty()) {
            World.world.logger.info("[DEBUG STARS] Map {} : {} groupe(s) ont recu des etoiles.", this.id, changedGroups.size());

            for (Monster.MobGroup group : changedGroups) {
                int oldInternal = oldStarsByGroupId.getOrDefault(group.getId(), 0);
                int newInternal = group.getStarBonus();
                int gainedInternal = newInternal - oldInternal;
                long oldUpdateAt = oldUpdateAtByGroupId.getOrDefault(group.getId(), now);
                long elapsedMs = Math.max(0L, now - oldUpdateAt);
                long requiredDelayMs = computeStarDelayPerPointMillis(oldInternal);
                int oldVisible = MobGroupStarProgression.toVisibleStars(oldInternal);
                int newVisible = MobGroupStarProgression.toVisibleStars(newInternal);
                String phase = oldInternal < MobGroupStarProgression.VISIBLE_UNIT ? "phase 0->1*" : "phase 1->10*";

                World.world.logger.info("[DEBUG STARS] map={} group={} cell={} starsInternal {}->{} (visible {}->{}) gained={} elapsed={}ms required={}ms reason=elapsed>=required ({})",
                        this.id,
                        group.getId(),
                        group.getCellId(),
                        oldInternal,
                        newInternal,
                        oldVisible,
                        newVisible,
                        gainedInternal,
                        elapsedMs,
                        requiredDelayMs,
                        phase);
            }
        }

        if (!changedGroups.isEmpty() && !this.getPlayers().isEmpty()) {
            for (Monster.MobGroup group : changedGroups) {
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this, group.getId());
                SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
            }
        }
    }

    private boolean shouldTraceStars() {
        return Main.modDebug && this.id == STAR_DEBUG_TRACE_MAP_ID;
    }

    private long computeStarDelayPerPointMillis(int currentInternalStars) {
        return MobGroupStarProgression.requiredDelayForCurrentInternalStars(currentInternalStars);
    }

    /**
     * Sauvegarde les étoiles actuelles des groupes de monstres
     * Cette méthode est appelée au redémarrage pour persister l'état des groupes
     */
    public void saveGroupsStars() {
        this.savedGroupsStars.clear();
        for (Monster.MobGroup group : this.mobGroups.values()) {
            if (group != null && group.getStarBonus() > 0) {
                this.savedGroupsStars.put(group.getCellId(), new MobGroupStarProgression.Snapshot(group.getStarBonus(), group.getLastStarBonusUpdateAt()));
            }
        }
    }

    /**
     * Restaure les étoiles sauvegardées des groupes après leur création
     * Les étoiles sont restaurées par cellID du groupe
     */
    public void restoreGroupsStars() {
        if (this.savedGroupsStars.isEmpty())
            return;

        long now = System.currentTimeMillis();

        for (Monster.MobGroup group : this.mobGroups.values()) {
            if (group != null) {
                MobGroupStarProgression.Snapshot snapshot = this.savedGroupsStars.get(group.getCellId());
                if (snapshot != null && snapshot.getInternalStars() > 0) {
                    group.resetStarBonus(snapshot.getInternalStars(), snapshot.getLastUpdateAt());
                } else {
                    group.resetStarBonus(0, now);
                }
            }
        }

        this.savedGroupsStars.clear();
    }

    /**
     * Définit les étoiles sauvegardées (par cellID)
     * Utilisé lors du redémarrage pour transférer l'état
     */
    public void setSavedGroupsStars(Map<Integer, MobGroupStarProgression.Snapshot> savedStars) {
        this.savedGroupsStars = new HashMap<>(savedStars);
    }

    /**
     * Récupère les étoiles sauvegardées
     */
    public Map<Integer, MobGroupStarProgression.Snapshot> getSavedGroupsStars() {
        return this.savedGroupsStars;
    }
}



