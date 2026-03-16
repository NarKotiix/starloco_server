package org.starloco.locos.database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.dynamics.AbstractDAO;
import org.starloco.locos.game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChallengeData extends AbstractDAO<Object> {

    private static final String LOAD_SQL = "SELECT `id`, `gainXP`, `gainDrop`, `gainParMob`, `conditions` FROM `challenge`";

    public ChallengeData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData(LOAD_SQL);
            ResultSet rs = result.resultSet;
            while (rs.next()) {
                World.world.addChallenge(
                        rs.getInt("id") + "," +
                                rs.getInt("gainXP") + "," +
                                rs.getInt("gainDrop") + "," +
                                rs.getInt("gainParMob") + "," +
                                rs.getInt("conditions")
                );
            }
        } catch (SQLException e) {
            super.sendError("ChallengeData#load", e);
        } finally {
            close(result);
        }
    }
}