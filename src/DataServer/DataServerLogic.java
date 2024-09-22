package DataServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author
 * Thread for Data base Server logic
 */
public class DataServerLogic implements Runnable{
    private final Socket socket;
    private final ItemTable itemTable;
    private final PrintWriter out;
    private final BufferedReader in;

    /**
     * Constructor
     * @param socket client socket
     * @param itemTable itemtable
     * @throws IOException Exception with client socket
     */
    public DataServerLogic(Socket socket, ItemTable itemTable) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.itemTable = itemTable;
    }

    /**
     * Commands for client to use
     */
    private enum Commands {
        GETITEMS
    }

    /**
     * Processed input from client
     */
    @Override
    public void run() {
        String inputLine;

        try {
            inputLine = in.readLine();
            System.out.println(inputLine);
            String[] input = inputLine.split(" ");
            if (input[0].equalsIgnoreCase(Commands.GETITEMS.name())) {
                if (input[1].matches("\\d+")) {
                    JSONArray jsonArray = new JSONArray();
                    ArrayList<Item> items = itemTable.getAll();
                    int start = ThreadLocalRandom.current().nextInt(items.size());
                    int i = 0;
                    int n = Integer.parseInt(input[1]);
                    // get sub-items and package them in json array
                    while (i < n) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            Item item = items.get((start + i) % items.size());
                            jsonObject.put("ItemID", item.getID());
                            jsonObject.put("name", item.getName());
                            jsonObject.put("price", item.getPrice());
                            jsonArray.put(jsonObject);
                            i++;
                        }
                        catch (JSONException e) {
                            System.out.println("Cannot convert item to json");
                        }
                    }
                    // send items
                    out.println(jsonArray.toString());
                }
            }
        }
        catch (IOException e) {
            out.println("Cannot read line");
            System.out.println("Cannot read line");
        }

        // Close connection after finishing request
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}