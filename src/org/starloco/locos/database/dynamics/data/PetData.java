package org.starloco.locos.database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.starloco.locos.database.dynamics.AbstractDAO;
import org.starloco.locos.entity.pet.PetEntry;
import org.starloco.locos.game.world.World;

public class PetData extends AbstractDAO<PetEntry> {

    public PetData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(PetEntry pets) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `petsOwner` SET `lastEatDate` = ?, `quantityEat` = ?, `pdv` = ?, `corpulence` = ?, `isEPO` = ? WHERE `id` = ?;");
            p.setLong(1, pets.getLastEatDate());
            p.setInt(2, pets.getQuaEat());
            p.setInt(3, pets.getPdv());
            p.setInt(4, pets.getCorpulence());
            p.setInt(5, (pets.getIsEupeoh() ? 1 : 0));
            p.setInt(6, pets.getObjectId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("PetData update", e);
        } finally {
            close(p);
        }
        return false;
    }

    public int load() {
        Result result = null;
        int i = 0;
        try {
            result = getData("SELECT * FROM `petsOwner`;");
            if (result == null || result.resultSet == null) {
                super.sendError("PetData load", new NullPointerException("Result or ResultSet is null"));
                return 0;
            }

            ResultSet RS = result.resultSet;
            while (RS.next()) {
                i++;
                int id          = RS.getInt("id");
                int template    = RS.getInt("template");
                long lastEat    = RS.getLong("lastEatDate");
                int qty         = RS.getInt("quantityEat");
                int pdv         = RS.getInt("pdv");
                int corpulence  = RS.getInt("corpulence");
                boolean isEpo   = (RS.getInt("isEPO") == 1);

                PetEntry entry = new PetEntry(
                        id,
                        template,
                        lastEat,
                        qty,
                        pdv,
                        corpulence,
                        isEpo
                );

                if (World.world == null) {
                    super.sendError("PetData load", new NullPointerException("World.world is null, cannot add PetEntry id=" + id));
                    continue;
                }

                World.world.addPetsEntry(entry);
            }
        } catch (Exception e) { // SQLException + NPE
            super.sendError("PetData load", e);
        } finally {
            close(result);
        }
        return i;
    }

    public void add(int id, long lastEatDate, int template) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("INSERT INTO `petsOwner`(`id`, `template`, `lastEatDate`, `quantityEat`, `pdv`, `corpulence`, `isEPO`) VALUES (?, ?, ?, ?, ?, ?, ?);");
            p.setInt(1, id);
            p.setInt(2, template);
            p.setLong(3, lastEatDate);
            p.setInt(4, 0);
            p.setInt(5, 10);
            p.setInt(6, 0);
            p.setInt(7, 0);
            execute(p);
        } catch (SQLException e) {
            super.sendError("PetData add", e);
        } finally {
            close(p);
        }
    }

    public void delete(int id) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM `petsOwner` WHERE `id` = ?");
            p.setInt(1, id);
            execute(p);
        } catch (SQLException e) {
            super.sendError("PetData delete", e);
        } finally {
            close(p);
        }
    }
}
