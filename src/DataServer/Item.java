package DataServer;

import org.json.JSONException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author
 * Item class for making auction items
 */
public class Item {

    private int ID;
    private String name;
    private double price;
    private final static String SQLcreate = "(ItemID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name CHAR(100), " + "price FLOAT)";

    /**
     * Constructor, create a random list of items (in term of json object)
     * @throws JSONException Exception when creating json
     */
    public Item(String name, double price){
        this.name = name;
        this.price = price;
    }

    /**
     * Constructor, use data from database to create Item
     * @param result result row from database
     * @throws SQLException Exception when getting result
     */
    public Item(ResultSet result) throws SQLException {
        int db_ID;
        String db_name;
        double db_price;

        db_ID = result.getInt("ItemID");
        db_name = result.getString("Name");
        db_price = result.getDouble("Price");

        this.ID = db_ID;
        this.name = db_name;
        this.price = db_price;
    }

    /**
     * Getter for ID
     * @return item ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Getter for item name
     * @return item name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for price
     * @return item price
     */
    public double getPrice() {
        return price;
    }

    /**
     * toString method for item
     * @return full info of item
     */
    public String toString() {
        return String.format("ItemID %d,name %s,price %.2f",
                getID(), getName(), getPrice());
    }

    /**
     * Create components inside database if it is empty
     * @return initial prepared statement
     */
    public static String getSQLcreate() {
        return SQLcreate;
    }

    /**
     * bind values to database column
     * @param ps prepared statement
     * @throws SQLException
     */
    public void bindVars(PreparedStatement ps) throws SQLException {
        ps.setString(1, name);
        ps.setDouble(2, price);
    }

    /**
     * Statement for inserting item into database
     * @param table table name
     * @return prepared statement for insert
     */
    public String getInsertPSString(String table) {
        return "INSERT INTO " + table + "(name, price)"
                + "values (?,?);";
    }

    /**
     * Statement for selecting item from database table
     * @param table table name
     * @return prepared statement for select
     */
    public static String getSelectString(String table) {
        return String.format("SELECT ItemID, name, price from %s", table);
    }
}