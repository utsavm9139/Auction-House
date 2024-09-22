package DataServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author
 * Data server class where accepting client connection
 */
public class dataServer {

    private static boolean debug = true;

    /**
     * Connection to database
     * @return Connection
     */
    public static Connection dbConnect() {
        Connection conn = null;
        String protocol = "jdbc:sqlite:";
        String dbName = "src/DataServer/data.db";
        String connString = protocol + dbName;
/**
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        }
        catch (SQLException ex) {
            System.err.println("dbConnect: Received ClassNotFoundException when trying "
                    + "to start SQLite: " + ex.getMessage());
            System.exit(1);
        }**/

        // Print database info
        try {
            conn = DriverManager.getConnection(connString);
            DatabaseMetaData dm = conn.getMetaData();
            if (debug) {
                System.out.println("dbConnect: Connected to database " + connString);
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
            }
        }
        catch (SQLException ex) {
            System.err.println("Received SQLException when trying to open db: "
                    + connString + " " + ex.getMessage());
            System.err.println("Connection string: " + connString);
            System.exit(1);
        }
        return conn;
    }

    /**
     * Main class
     * @param args input args (port number to start up server)
     */
    public static void main(String args[]) {
        Connection db = dbConnect();
        ItemTable itemTable = new ItemTable(db);
        ServerSocket serverSocket = null;
        System.out.println("Table contents:");
        for (Item item : itemTable.getAll()) {
            System.out.println(item.toString());
        }

        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        }
        catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // accepting client and put them in Logic
        while (true) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("A client just connected");
                DataServerLogic dl = new DataServerLogic(client, itemTable);
                Thread t = new Thread(dl);
                t.start();
            }
            catch (IOException e) {
                System.out.println("Exception when accepting client");
            }
        }
    }
}