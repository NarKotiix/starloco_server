package org.starloco.locos.database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.client.Player;
import org.starloco.locos.database.dynamics.AbstractDAO;
import org.starloco.locos.entity.Collector;
import org.starloco.locos.game.world.World;
import org.starloco.locos.area.map.GameMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CollectorData extends AbstractDAO<Collector> {

    private static final String UPDATE_SQL = "UPDATE `percepteurs` SET `objets` = ?, `kamas` = ?, `xp` = ? WHERE guid = ?";
    private static final String LOAD_SQL   = "SELECT * FROM `percepteurs`";
    private static final String DELETE_SQL = "DELETE FROM `percepteurs` WHERE guid = ?";
    private static final String INSERT_SQL = "INSERT INTO `percepteurs` VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String GET_ID_SQL = "SELECT `guid` FROM `percepteurs` ORDER BY `guid` ASC LIMIT 0, 1";

    public CollectorData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
        // unused — bulk load handled by load()
    }

    @Override
    public boolean update(Collector collector) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement(UPDATE_SQL);
            p.setString(1, collector.parseItemCollector());
            p.setLong(2, collector.getKamas());
            p.setLong(3, collector.getXp());
            p.setInt(4, collector.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("CollectorData#update", e);
        } finally {
            close(p);
        }
        return false;
    }

    public int load() {
        Result result = null;
        int count = 0;
        try {
            result = getData(LOAD_SQL);
            ResultSet rs = result.resultSet;
            while (rs.next()) {
                GameMap map = World.world.getMap(rs.getShort("mapid"));
                if (map == null) continue;

                int poseurId = rs.getInt("poseur_id");
                Player player = (poseurId > 0) ? World.world.getPlayer(poseurId) : null;

                String dateStr = rs.getString("date");
                long time = (dateStr != null && !dateStr.isEmpty())
                        ? Long.parseLong(dateStr)
                        : 0L;

                World.world.addCollector(new Collector(
                        rs.getInt("guid"),
                        rs.getShort("mapid"),
                        rs.getInt("cellid"),
                        rs.getByte("orientation"),
                        rs.getInt("guild_id"),
                        rs.getShort("N1"),
                        rs.getShort("N2"),
                        player,
                        time,
                        rs.getString("objets"),
                        rs.getLong("kamas"),
                        rs.getLong("xp")
                ));
                count++;
            }
        } catch (SQLException e) {
            super.sendError("CollectorData#load", e);
        } finally {
            close(result);
        }
        return count;
    }

    public boolean delete(int id) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement(DELETE_SQL);
            p.setInt(1, id);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("CollectorData#delete", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean add(int guid, int mapid, int guildId, int poseurId,
                       long date, int cellid, int orientation, short n1, short n2) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement(INSERT_SQL);
            p.setInt(1, guid);
            p.setInt(2, mapid);
            p.setInt(3, cellid);
            p.setInt(4, orientation);
            p.setInt(5, guildId);
            p.setInt(6, poseurId);
            p.setString(7, Long.toString(date));
            p.setShort(8, n1);
            p.setShort(9, n2);
            p.setString(10, "");
            p.setLong(11, 0L);
            p.setLong(12, 0L);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("CollectorData#add", e);
        } finally {
            close(p);
        }
        return false;
    }

    public int getId() {
        Result result = null;
        int id = -10000; // valeur par défaut sûre, évite les conflits NPC
        try {
            result = getData(GET_ID_SQL);
            ResultSet rs = result.resultSet;
            if (rs.next()) {
                int guid = rs.getInt("guid") - 1;
                id = (guid < -9999) ? guid : -10000;
            }
        } catch (SQLException e) {
            super.sendError("CollectorData#getId", e);
        } finally {
            close(result);
        }
        return id;
    }
}