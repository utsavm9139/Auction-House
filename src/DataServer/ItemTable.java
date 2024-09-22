package DataServer;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author
 * Class for statements comunicating to item table in database
 */
public class ItemTable {
    private Connection db;
    private final String tableName = "item";
    private final String creatSQL;

    /**
     * Constructor
     * @param db database connection
     */
    public ItemTable(Connection db) {
        if (db == null) {
            throw new IllegalArgumentException("AuctionCollection constructor: " +
                    "db connection is null");
        }
        this.db = db;
        this.creatSQL = "create table item" + Item.getSQLcreate();
    }

    /**
     * Insert method
     * @param item item
     * @return true if successful, false otherwise
     */
    public boolean insert(Item item) {
        PreparedStatement ps = null;
        String sql = item.getInsertPSString(tableName);
        int nInserted = 0;

        try {
            ps = db.prepareStatement(sql);
            item.bindVars(ps);
            nInserted = ps.executeUpdate();
        } catch (SQLException e) {
            System.err.printf("insert: Received SQLException when trying to " +
                    "create or execute statement: %s", e.getMessage());
            System.err.println("SQL: " + sql);
            System.exit(1);
        }

        return nInserted == 1;
    }

    /**
     * Get all items from item table in database
     * @return arraylist of item
     */
    public ArrayList<Item> getAll() {
        ArrayList<Item> items = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet result = null;
        String sql = Item.getSelectString(tableName);

        try {
            ps = db.prepareStatement(sql);
            result = ps.executeQuery();
            while (result.next()) {
                items.add(new Item(result));
            }
            result.close();
        }
        catch (SQLException e) {
            System.err.printf("getAll: Received SQLException when trying to " +
                            "create or execute statement: %s",
                    e.getMessage());
            System.err.println("SQL: " + sql);
            System.exit(1);
        }

        return items;
    }

    /**
     * Delete item table in database.
     */
    public void dropTable() {
        PreparedStatement ps = null;
        String sql = "DROP TABLE item";
        try {
            ps = db.prepareStatement(sql);
            ps.execute();
        }
        catch (SQLException e) {
            System.err.println("dropTable: Received SQLException when trying " +
                    "to create or execute statement: "
                    + e.getMessage());
            System.err.println("SQL: " + sql);
            System.exit(1);
        }
    }

    /**
     * Create item table in database
     */
    public void createTable() {
        PreparedStatement ps = null;

        try {
            ps = db.prepareStatement(creatSQL);
            ps.execute();
        }
        catch (SQLException ex) {
            System.err.println("createTable: Received SQLException when trying" +
                    " to create or execute statement: "
                    + ex.getMessage());
            System.err.println("SQL: " + creatSQL);
            System.exit(1);
        }
    }

    /**
     * Check if item table is in database
     * @return true if it is, false otherwise
     */
    public boolean checkTableExits() {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' " +
                "AND name='" + tableName + "';";
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            ps = db.prepareStatement(sql);
            if (ps.execute()) {
                result = ps.getResultSet();
                if (!result.isClosed()) {
                    result.next();
                    if (result.getRow() >= 0) {
                        return result.getString(1).equals(tableName);
                    }
                }
                else {
                    System.out.println("result set is closed.");;
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("checkTableExists: Received SQLException "
                    + "when trying to create or execute: " + sql);
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        return false;
    }

}