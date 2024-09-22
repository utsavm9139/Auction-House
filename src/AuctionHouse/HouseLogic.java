package AuctionHouse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * @author
 * House logic, act as a customer service dealing with bidding
 */
public class HouseLogic implements Runnable{
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private AuctionCollection collection;
    private final ArrayList<Socket> agentList;
    private final int clientID;
    private final HouseClient houseClient;

    /**
     * Constructor
     * @param clientSocket client socket
     * @param clientID client ID
     * @param collection collection database
     * @param agentList list of connected agent
     * @param houseClient socket connecting between house and bank
     * @throws IOException Exception when setting up in and out
     */
    public HouseLogic(Socket clientSocket, int clientID, AuctionCollection collection,
                      ArrayList<Socket> agentList, HouseClient houseClient) throws IOException {
        this.clientSocket = clientSocket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.collection = collection;
        this.agentList = agentList;
        this.clientID = clientID;
        this.houseClient = houseClient;
    }

    /**
     * Auction house commands
     */
    private enum Commands {
        ECHO, BID, REQUESTITEMS
    }

    /**
     * Listener to agent's request
     */
    @Override
    public void run() {
        String inputLine = null;
        String outputLine;

        do {
            try {
                inputLine = in.readLine();

                if (inputLine != null) {
                    System.out.println(inputLine);

                    JSONObject json = validateRequest(inputLine);

                    if (json != null) {
                        outputLine = processRequest(json);
                        out.println(outputLine);
                    } else {
                        out.println("Invalid Request.");
                    }
                }
                else {
                    out.println("An agent just left");
                }
            } catch (IOException e) {
                inputLine = null;
                System.out.println("An agent just left");
            } catch (JSONException e) {
                out.println("Invalid Request.");
            }
        } while (inputLine != null);
    }

    /**
     * Validate whether request is a JSON String
     * @param inputLine input from sender
     * @return json object or null
     */
    private JSONObject validateRequest(String inputLine) {
        inputLine = "[" + inputLine + "]";
        JSONArray jsonArray;
        JSONObject jsonObject = null;
        try {
            jsonArray = new JSONArray(inputLine);
            jsonObject = jsonArray.getJSONObject(0);
        } catch (JSONException e) {
            System.err.println("Invalid JSON request " + e);
        }
        return jsonObject;
    }

    /**
     *
     * @param input request as jason object
     * @return output after process request
     * @throws JSONException Exception
     */
    private String processRequest(JSONObject input) throws JSONException {
        String command = null;
        String output = null;
        command = input.getString("MessageType");

        if (command.equalsIgnoreCase(Commands.ECHO.name())) {
            output = "ECHO";
        }
        else if (command.equalsIgnoreCase(Commands.BID.name())) {

            System.out.print("Bid Status: ");

            int itemID = input.getInt("ItemID");
            JSONObject item = collection.getItem(itemID);
            if (item == null) {
                // Check invalid item
                System.out.println("Rejection - Invalid ID");
                output = "Item is not in the list.";
            }
            else {
                // Check availability of item

                if (!(item.getString("State").matches("AVAILABLE|BIDDING"))) {
                    System.out.println("Rejection - Invalid ID");
                    output = "Item " + itemID + " is not available.";
                }
                else {
                    // Check bid amount
                    double bid = input.getDouble("Amount");
                    double current_bid = item.getDouble("CurrentBid");

                    if (current_bid < bid ) {
                        JSONObject json = new JSONObject();
                        json.put("SenderType","AUCTION");
                        json.put("MessageType","BLOCK");
                        json.put("AccountID", input.getInt("AccountID"));
                        json.put("Amount", bid);
                        try {
                            // Check with the bank if fund is enough to bid
                            PrintWriter out = new PrintWriter(
                                    houseClient.getSocket().getOutputStream(), true);
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(houseClient
                                            .getSocket().getInputStream()));
                            out.println(json.toString());

                            // Getting bank response
                            String inputLine = in.readLine();

                            String item_description = "ItemID " + itemID + " " +
                                    item.getString("Name") + " : " +
                                    item.getDouble("CurrentBid") +
                                    " initial price " + item.getDouble("InitialPrice");

                            // Change new winner if acceptance
                            if (inputLine.equals("Successful")) {
                                int old_winner = item.getInt("Winner");
                                int old_accountID = item.getInt("AccountID");
                                double old_bid = item.getDouble("CurrentBid");

                                // Update new winner for the item
                                item.remove("Winner");
                                item.put("Winner", clientID);
                                item.remove("AccountID");
                                item.put("AccountID",input.getInt("AccountID"));
                                item.remove("CurrentBid");
                                item.put("CurrentBid", bid);

                                item_description = "ItemID " + itemID + " " +
                                        item.getString("Name") + " : " +
                                        item.getDouble("CurrentBid") +
                                        " initial price " + item.getDouble("InitialPrice");

                                System.out.println("Acceptance");
                                output = "Acceptance," + item_description;

                                String finalItem_description = item_description;

                                // create or reset timer to 30s if new higher bid
                                TimerTask task = setWinnerTimer(item,finalItem_description);
                                collection.setTimerAt(itemID, task);

                                // notify old winner outbid and request the bank to unblock fund
                                if (old_winner != -1) {
                                    outBidMessage(old_winner,item_description);
                                    JSONObject jsonToBank = new JSONObject();
                                    jsonToBank.put("SenderType","AUCTION");
                                    jsonToBank.put("MessageType","UNBLOCK");
                                    jsonToBank.put("AccountID",old_accountID);
                                    jsonToBank.put("Amount", old_bid);

                                    // Check if able to unblock
                                    out.println(jsonToBank.toString());
                                    inputLine = in.readLine();
                                    if (inputLine.equals("Successful")) {
                                        unblockMessage(old_winner, old_bid);
                                    }
                                    else {
                                        System.out.println(inputLine);
                                    }
                                }
                            }
                            else {
                                System.out.println("Rejection - Insufficient funds");
                                output = "Rejection," + "Insufficient funds,"
                                        + item_description;
                            }
                        } catch (IOException e) {
                            output = "Cannot request checking fund from the bank";
                        }
                    }
                    else {
                        System.out.println("Rejection - Low bid");
                        output = "Rejection,Your bid on ItemID " + itemID +
                                " is too low. Current bid is " + current_bid;
                    }
                }
            }
        }
        else if (command.equalsIgnoreCase(Commands.REQUESTITEMS.name())) {
            output = collection.toString();
        }
        else {
            output = "Invalid Command";
        }
        return output;
    }

    /**
     * Set timer for winner notification
     * @param item item
     * @param finalItem_description item description
     * @return task
     */
    private TimerTask setWinnerTimer(JSONObject item, String finalItem_description) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // notify winner
                PrintWriter out;
                try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    System.err.println("Cannot get output stream: " + e);
                    return;
                }
                // send notification with house accountid and debt amount
                try {
                    out.println("Winner," + finalItem_description + "," +
                            houseClient.getAccountID() + "," + item.getDouble("CurrentBid"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                item.remove("State");
                try {
                    item.put("State", "SOLD");
                    if (collection.isAllSold()) {
                        System.out.println("Items are all sold");
                    }
                } catch (JSONException e) {
                    System.err.println("Exception when set state of item to SOLD" + e);
                }
            }
        };
        return task;
    }

    /**
     * Notify an agent if his/her bid is out-bidded
     * @param old_winner old winner
     * @param item_description item with new bid
     * @throws IOException Exception when sending message
     */
    private void outBidMessage(int old_winner, String item_description) throws IOException {
        Socket socket = agentList.get(old_winner);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("Outbid," + item_description);
    }

    /**
     * If outbid, old winner will have their bidded fund unblocked
     * @param old_winner old winner
     * @param amount blocked amount needed to be unblocked
     * @throws IOException Exception when sending message
     */
    private void unblockMessage(int old_winner, double amount) throws IOException {
        Socket socket = agentList.get(old_winner);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("Your fund " + "[" + amount + "] " + "is unblocked");
    }
}