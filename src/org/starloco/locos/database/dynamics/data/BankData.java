package org.starloco.locos.database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.client.Account;
import org.starloco.locos.database.dynamics.AbstractDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankData extends AbstractDAO<Object> {

    private static final String ADD_SQL = "INSERT INTO `banks` (`id`, `kamas`, `items`) VALUES (?, 0, '')";
    private static final String UPDATE_SQL = "UPDATE `banks` SET `kamas` = ?, `items` = ? WHERE `id` = ?";
    private static final String GET_SQL = "SELECT `kamas`, `items` FROM `banks` WHERE `id` = ?";

    public BankData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public boolean add(int guid) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement(ADD_SQL);
            p.setInt(1, guid);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("BankData#add", e);
        } finally {
            close(p);
        }
        return false;
    }

    public void update(Account account) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement(UPDATE_SQL);
            p.setLong(1, account.getBankKamas());
            p.setString(2, account.parseBankObjectsToDB());
            p.setInt(3, account.getId());
            execute(p);
        } catch (SQLException e) {
            super.sendError("BankData#update", e);
        } finally {
            close(p);
        }
    }

    public String get(int guid) {
        Result result = null;
        try {
            result = getData("SELECT `kamas`, `items` FROM `banks` WHERE `id` = " + guid);
            ResultSet rs = result.resultSet;
            if (rs.next()) {
                return rs.getLong("kamas") + "@" + rs.getString("items");
            }
        } catch (SQLException e) {
            super.sendError("BankData#get", e);
        } finally {
            close(result);
        }
        return null;
    }
}