package bank;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author
 * Bank logic, act as a Banker dealing with a client
 */
public class BankLogic implements Runnable{
    private final Bank bank;
    private final Socket clientSocket;
    private String senderType;
    private int accountID;
    private final ArrayList<String> auctionInfo;
    private final ArrayList<Socket> auctionList;
    private final ArrayList<Socket> agentList;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructor
     * @param bank bank
     * @param clientSocket client
     * @param accountID bank account id
     * @param auctionInfo host name and port number of auction houses
     * @param auctionList list of auction socket client
     * @param agentList list of agent socket client
     * @throws IOException Exception
     */
    public BankLogic(Bank bank, Socket clientSocket, int accountID,
                     ArrayList<String> auctionInfo,
                     ArrayList<Socket> auctionList,
                     ArrayList<Socket> agentList) throws IOException {
        this.bank = bank;
        this.clientSocket = clientSocket;
        senderType = "UNKNOWN";
        this.accountID = accountID;
        this.auctionInfo = auctionInfo;
        this.auctionList = auctionList;
        this.agentList = agentList;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    /**
     * Bank commands
     */
    private enum Commands {
        ECHO, OPENACCOUNT, CHECKFUNDS, AUCTIONINIT, BLOCK, UNBLOCK,
        AUCTIONLIST, TRANSFERFUNDS
    }

    /**
     * Listening to request from a client
     */
    @Override
    public void run() {
        String inputLine = null;
        String outputLine;

        do {
            try {
                inputLine = in.readLine();

                if (inputLine != null) {
                    // Validate request from client
                    System.out.println(inputLine);
                    JSONObject json = validateRequest(inputLine);

                    // Process if valid, print error otherwise
                    if (json != null) {
                        outputLine = processRequest(json);
                        out.println(outputLine);
                    } else {
                        out.println("Invalid Request.");
                    }
                }
                else {
                    clientSocket.close();
                    // Remove socket if disconnected (null inputLine)
                    if (senderType.equals("AGENT")) {
                        agentList.remove(clientSocket);
                        System.out.println("An agent just left");
                    }
                    else if (senderType.equals("AUCTION")) {
                        int i = auctionList.indexOf(clientSocket);
                        auctionList.remove(clientSocket);
                        auctionInfo.remove(i);
                        System.out.println("An auction house just left");

                        // Notify all agents when a house left
                        sendListToAgent();
                    }
                    else {
                        System.out.println("An unknown just left");
                    }
                }

            } catch (IOException e) {
                inputLine = null;
            } catch (JSONException e) {
                out.println("Cannot read request/Missing Info");
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
        JSONObject jsonObject;
        try {
            jsonArray = new JSONArray(inputLine);
            jsonObject = jsonArray.getJSONObject(0);
        } catch (JSONException e) {
            jsonObject = null;
        }
        return jsonObject;
    }

    /**
     * Process request from sender
     * @param input JSON object
     * @return result after request processed
     * @throws JSONException error when read JSON object
     */
    private String processRequest(JSONObject input) throws JSONException, IOException {
        String command;
        String output = "";

        // Get command request from client
        try {
            command = input.getString("MessageType");
        } catch (JSONException e) {
            return "Missing MessageType";
        }

        // Process each command
        if (command.equalsIgnoreCase(Commands.ECHO.name())) {
            output = "ECHO";
        }
        else if (command.equalsIgnoreCase(Commands.OPENACCOUNT.name())) {
            String name = input.getString("Name");
            double amount = input.getDouble("Amount");
            output = bank.openAccount(accountID, name, amount);

            // save info for agent, then send info of current opened houses
            senderType = "AGENT";
            agentList.add(clientSocket);

            output += ",";

            for (int i = 0; i < auctionInfo.size(); i++) {
                output += auctionInfo.get(i) + ",";
            }
        }
        else if (command.equalsIgnoreCase(Commands.CHECKFUNDS.name())) {
            output = bank.checkFunds(input.getInt("AccountID"));
        }
        else if (command.equalsIgnoreCase(Commands.AUCTIONINIT.name())) {
            output = bank.openAccount(accountID, "Auction",0);

            // save info for auction after open account, then notify agent
            senderType = "AUCTION";
            String info = input.getString("ExtraInfo");
            auctionInfo.add(info);
            auctionList.add(clientSocket);
            sendListToAgent();

        }
        else if (command.equalsIgnoreCase(Commands.AUCTIONLIST.name())) {
            output = "House list,";
            if (auctionInfo.size() == 0) {
                output += "No houses available";
            }
            else {
                for (String str : auctionInfo) {
                    output += str + ",";
                }
            }
        }
        else if (command.equalsIgnoreCase(Commands.BLOCK.name())) {
            int accountID = input.getInt("AccountID");
            double amount = input.getDouble("Amount");
            output = bank.blockFunds(accountID, amount);
        }
        else if (command.equalsIgnoreCase(Commands.UNBLOCK.name())) {
            int accountID = input.getInt("AccountID");
            double amount = input.getDouble("Amount");
            output = bank.unblockFunds(accountID, amount);
        }
        else if (command.equalsIgnoreCase(Commands.TRANSFERFUNDS.name())) {
            int toAccountID = input.getInt("AccountID");
            int fromAccountID = accountID;
            double amount = input.getDouble("Amount");
            output = bank.transferFunds(fromAccountID,toAccountID,amount) + "," + toAccountID;
        }
        else {
            output = "Unknown Command";
        }
        return output;
    }

    /**
     * Send list of current opening auction houses
     * @throws IOException Exception
     */
    private void sendListToAgent() throws IOException {
        if (agentList.size() == 0) return;

        for (Socket s : agentList) {
            PrintWriter out = new PrintWriter(s.getOutputStream(),true);
            String output = "House list,";

            if (auctionInfo.size() != 0) {
                for (String str : auctionInfo) {
                    output += str + ",";
                }
            }
            else {
                output += "No houses available";
            }
            out.println(output);
        }
    }
}