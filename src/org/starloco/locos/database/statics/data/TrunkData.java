package org.starloco.locos.database.statics.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.starloco.locos.database.Database;
import org.starloco.locos.database.statics.AbstractDAO;
import org.starloco.locos.game.world.World;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.area.map.entity.Trunk;

public class TrunkData extends AbstractDAO<Trunk> {

    public TrunkData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

	@Override
	public void load(Object obj)
	{
    }

	@Override
	public boolean update(Trunk t)
	{
		return false;
	}

	public int load() {
		Result result = null;
		int nbr = 0;
		try {
			result = getData("SELECT * from coffres");
			ResultSet RS = result.resultSet;
			while (RS.next()) {
				World.world.addTrunk(new Trunk(RS.getInt("id"), RS.getInt("id_house"), RS.getShort("mapid"), RS.getInt("cellid")));
				nbr++;
			}
		} catch (SQLException e) {
			super.sendError("CoffreData load", e);
		} finally {
			close(result);
		}
		return nbr;
	}

    public boolean insert(Trunk trunk) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("INSERT INTO `coffres` (`id`, `id_house`, `mapid`, `cellid`) " +
                    "VALUES (?, ?, ?, ?)");
            p.setInt(1, trunk.getId());
            p.setInt(2, trunk.getHouseId());
            p.setInt(3, trunk.getMapId());
            p.setInt(4, trunk.getCellId());
            execute(p);

            return Database.getDynamics().getTrunkData().insert(trunk);
        } catch (SQLException e) {
            super.sendError("Coffre insert", e);
        } finally {
            close(p);
        }
        return false;
    }

    public int getNextId() {
        Result result = null;
        int guid = -1;
        try {
            result = getData("SELECT MAX(id) AS max FROM `coffres`");
            ResultSet RS = result.resultSet;

            if (RS.next()) {
                int max = RS.getInt("max");
                guid = (RS.wasNull() ? 1 : max + 1);
            } else {
                guid = 1;
            }
        } catch (SQLException e) {
            super.sendError("CoffreData getNextId", e);
        } finally {
            close(result);
        }
        return guid;
    }
}
