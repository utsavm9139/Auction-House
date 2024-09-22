package AGent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * class Agent
 */
public class Agent {

    private static final BufferedReader stdIn = new BufferedReader(
            new InputStreamReader(System.in));
    private static final HashMap<String, Socket> socketList = new HashMap<>();
    private static JSONObject jsonObject = new JSONObject();
    private static AgentHouseLogic AHLogic = null;
    private static AgentBankLogic ABLogic = null;
    private static PrintWriter outTmp = null;
    private static PrintWriter out = null;
    private static int bankAccountID = -1;
    private static int isBidding = 0;
    private static HashMap<Integer, Double> invoice = new HashMap<>();
    private static String[] name = {"name1", "name2",
            "name3"};

    /**
     * main function for agent include all the logic
     * @param args argument: hostname and port number
     * @throws IOException
     * @throws InterruptedException
     * @throws JSONException
     */
    public static void main(String[] args) throws IOException,
            InterruptedException, JSONException {

        //split argument into string hostname and int port number
        String bank_hostName = args[0];
        int bank_portNumber = Integer.parseInt(args[1]);

        //make color (j4f)
        System.out.println("Connecting to the Bank...");
        TimeUnit.MILLISECONDS.sleep(500);
        System.out.println("Successfully connected.");

        /*
         * Agent: ask name, amount, then create json object,
         * send to bank to create account
         */
        jsonObject.put("SenderType", "AGENT");
        jsonObject.put("MessageType", "OPENACCOUNT");

        System.out.println("Hello sir! what's your name?");
        //get name
        jsonObject.put("Name", stdIn.readLine());
        System.out.println("Nice to meet you " +
                jsonObject.getString("Name") +
                ". I'm your Agent, " + getNextName() + ".");
        System.out.println("It's a pleasure to meet you.");

        String amount;
        do {
            System.out.println("Before we get you started on bidding, " +
                    "I have to ask, how much money do you have?");
            //get amount
            amount = stdIn.readLine();
            if (amount.matches("\\d+")) {
                break;
            }
            else {
                System.out.println("Invalid amount");
            }
        } while (true);
        jsonObject.put("Amount", amount);


        // Connect to the bank
        try {
            Socket socket = new Socket(bank_hostName, bank_portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            //send jsonObject request to bank to create account
            out.println(jsonObject.toString());
            //get input from bank
            String fromBank = in.readLine();
            String[] inputLine = fromBank.split(",");
            //print out all the list of house that available
            System.out.println("Here are the list of all auction houses:");
            for (int i = 0; i < inputLine.length; i++) {
                if (i == 0) {
                    //System.out.println(inputLine[i]);
                    String[] line = inputLine[i].split(" ");

                    //bank account ID
                    bankAccountID = Integer.parseInt(line[3]);
                    if (inputLine.length < 2) {
                        System.out.println("No Houses are currently open");
                    }
                }
                else {
                    /*if (!socketList.containsKey(inputLine[i])) {
                        connectToHouse(inputLine[i], socketList);
                    }*/
                    System.out.println("House No." + i);
                }
            }
            //listener to bank
            ABLogic = new AgentBankLogic(socket, socketList, invoice);
            Thread t = new Thread(ABLogic);
            t.start();
        } catch (IOException e) {
            System.err.println("Cannot connect to the bank. " + e);
        }

        /*
         * Agent: deal with cases from user input from main menu
         */
        do {
            String option;
            do {
                TimeUnit.MILLISECONDS.sleep(100);
                showMainMenu();
                //get option from user
                option = stdIn.readLine();

                if (option == null) {
                    System.out.println("Bank is closed suddenly.");
                    break;
                } else if (!(option.matches("B|A|X"))) {
                    System.out.println("Invalid command [" + option + "].");
                } else {
                    break;
                }
            } while (true);

            switch (option) {
                //get balance from bank
                case "B": {
                    jsonObject = new JSONObject();
                    jsonObject.put("SenderType", "AGENT");
                    jsonObject.put("MessageType", "CHECKFUNDS");
                    jsonObject.put("AccountID", bankAccountID);
                    out.println(jsonObject.toString());

                    TimeUnit.MILLISECONDS.sleep(500);

                    if (AHLogic != null) {
                        if (!invoice.isEmpty()) {
                            System.out.println("Make payment? (Y/N)");
                            String fromUser = stdIn.readLine();
                            if (fromUser.matches("Y")) {
                                for (Map.Entry me : invoice.entrySet()) {
                                    jsonObject = new JSONObject();
                                    jsonObject.put("SenderType", "AGENT");
                                    jsonObject.put("MessageType", "TRANSFERFUNDS");
                                    jsonObject.put("AccountID", me.getKey());
                                    jsonObject.put("Amount", me.getValue());
                                    out.println((jsonObject.toString()));
                                }
                            }
                        }
                    }
                    break;
                }
                //contact auction house
                case "A": {
                    caseA();
                    break;
                }
                //exit
                case "X": {
                    //check if user is currently bidding any item
                    if (isBidding != 0) {
                        System.out.println("You are currently bidding, " +
                                "can not exit.");
                        break;
                    }
                    else if (AHLogic != null) {
                        if (AHLogic.isBidding()) {
                            System.out.println("You are currently bidding, " +
                                    "can not exit.");
                            break;
                        }
                    }
                    else {
                        System.exit(1);
                        break;
                    }
                }
                default: {
                    System.err.println("Impossible Reach");
                    System.exit(1);
                }
            }
        } while (true);
    }

    /**
     * show main menu for user
     */
    private static void showMainMenu() {
        System.out.println();
        System.out.println("Your command, Sir.");
        System.out.println("B: Get balance from the Bank");
        System.out.println("A: Contact Auction house.");
        System.out.println("X: Exit");
        System.out.println();
    }

    /**
     * shoe house menu for user after contact auction house
     */
    private static void showHouseMenu() {
        System.out.println();
        System.out.println("What would you like to do?");
        System.out.println("B: Bid");
        System.out.println("R: Request a list of items");
        System.out.println("X: Go back");
        System.out.println();
    }

    /**
     * function to contact auction house
     * send request auction house list to bank
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    public static void caseA() throws IOException,
            JSONException, InterruptedException {
        //send request house list to bank
        jsonObject = new JSONObject();
        jsonObject.put("SenderType", "AGENT");
        jsonObject.put("MessageType", "AUCTIONLIST");
        out.println(jsonObject.toString());
        TimeUnit.MILLISECONDS.sleep(500);

        //if (inputLine[0].equals("House list")) {
        //check what auction house is available
        if (socketList.isEmpty()) {
            System.out.println("No Houses are currently open, cannot connect");
        }
        else {
            do {
                //show house menu and get option
                showHouseMenu();
                String houseOption = stdIn.readLine();
                switch (houseOption) {
                    //bid
                    case "B": {
                        bidding(ABLogic.getInputLine());
                        return;
                    }
                    //request a list of item
                    case "R": {
                        requestItem(ABLogic.getInputLine());
                        return;
                    }
                    //go back
                    case "X": {
                        return;
                    }
                    default: {
                        System.out.println("Invalid command ["
                                + houseOption + "]");
                        break;
                    }
                }
            } while (true);
        }
    }

    /**
     * connecting to house
     * use thread to listen to house
     * @param auctionHouse
     * @param socketList
     */
    public static void connectToHouse(String auctionHouse,
                                      HashMap<String, Socket> socketList) {

        String[] inputLine = auctionHouse.split(" ");
        try {
            //System.out.println(auctionHouse);
            //System.out.println("Connecting to house....");
            Socket socket = new Socket(inputLine[0],
                    Integer.parseInt(inputLine[1]));
            socketList.put(auctionHouse, socket);
            //System.out.println("Successful connect to house");

            //is bidding
            isBidding = 0;
            //listener to house
            AHLogic = new AgentHouseLogic(socket,
                    socketList, auctionHouse, isBidding, invoice);
            Thread t = new Thread(AHLogic);
            t.start();
        } catch (IOException e) {
            System.out.println("Cannot connect to Auction House");
        }
    }

    /**
     * function for bidding
     * @param auctionHouse auction house key
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    public static void bidding (String[] auctionHouse)
            throws IOException, JSONException, InterruptedException {
        do {
            System.out.println("Which house? (X to cancel)");
            //System.out.print("House No.");
            //get house number from user
            String fromUser = stdIn.readLine();
            if (fromUser.matches("\\d+")) {
                int fromUserInt = Integer.parseInt(fromUser);
                if (fromUserInt > 0 && fromUserInt <= socketList.size()) {
                    listenToHouse(fromUser, auctionHouse);

                    do {
                        System.out.println("on what Item sir? " +
                                "(Go by Item No. , X to go back)");
                        //System.out.print("Item ");
                        //get item number from user
                        String itemID = stdIn.readLine();

                        if (itemID.matches("\\d+")) {
                            do {
                                System.out.println("and how much? " +
                                        "(X to go back)");
                                //System.out.print("$");
                                //get how much money user want to bid on item
                                String howMuch = stdIn.readLine();

                                if (howMuch.matches("\\d+") &&
                                        fromUserInt <= socketList.size()) {
                                    //send request
                                    jsonObject = new JSONObject();
                                    jsonObject.put("SenderType", "AGENT");
                                    jsonObject.put("MessageType", "BID");
                                    jsonObject.put("ItemID",
                                            Double.parseDouble(itemID));
                                    jsonObject.put("Amount",
                                            Double.parseDouble(howMuch));
                                    jsonObject.put("AccountID", bankAccountID);
                                    outTmp.println(jsonObject.toString());
                                    //let list of items print first
                                    TimeUnit.MILLISECONDS.sleep(500);
                                    return;
                                }
                                else if (howMuch.matches("X")) {
                                    break;
                                }
                                else {
                                    System.out.println("Invalid input");
                                }
                            } while (true);
                        }
                        else if (itemID.matches("X")) {
                            break;
                        }
                        else {
                            System.out.println("Invalid Item No." + itemID);
                        }
                    } while (true);
                }
            }
            else if (fromUser.matches("X")) {
                return;
            }
            else {
                System.out.println("Invalid House No." + fromUser);
            }
        }while (true);
    }

    /**
     * function request list of item from auction house
     * @param auctionHouse auction house key
     * @throws IOException
     * @throws InterruptedException
     * @throws JSONException
     */
    public static void requestItem (String[] auctionHouse)
            throws IOException, InterruptedException, JSONException {

        //System.out.println(auctionHouse[0]);
        do {
            System.out.println("From where? (give a HouseID)");
            System.out.print("House No.");
            //get house number from user
            String fromUser = stdIn.readLine();

            if (!fromUser.matches("\\d+")) {
                System.out.println("Invalid HouseID");
            }
            else if (Integer.parseInt(fromUser) > 0 &&
                    Integer.parseInt(fromUser) <= socketList.size()) {

                //listen to house
                listenToHouse(fromUser, auctionHouse);

                //send request
                jsonObject = new JSONObject();
                jsonObject.put("SenderType", "AGENT");
                jsonObject.put("MessageType", "REQUESTITEMS");
                outTmp.println(jsonObject.toString());

                //let list of items print first
                TimeUnit.MILLISECONDS.sleep(500);
                return;
            }
            else {
                System.out.println("HouseID not exist");
            }
        }while (true);
    }

    /**
     * functon listen to house when user request item
     * @param fromUser String input from user
     * @param auctionHouse auction house key
     * @throws IOException
     */
    public static void listenToHouse (String fromUser, String[] auctionHouse)
            throws IOException {

        int houseID = Integer.parseInt(fromUser);

        //connect to house again to make sure
        if (socketList.get(auctionHouse[houseID]) == null) {
            connectToHouse(auctionHouse[houseID], socketList);
        }

        for (int i = 1; i < auctionHouse.length; i++) {
            //System.out.println(auctionHouse[i]);
            if (houseID == i) {
                outTmp = new PrintWriter(socketList.get(auctionHouse[i])
                        .getOutputStream(), true);
                BufferedReader inTmp = new BufferedReader(
                        new InputStreamReader(socketList.get(auctionHouse[i])
                                .getInputStream()));
                //System.out.println("check check");
            }
        }
    }

    public static String getNextName () {
        int rand = ThreadLocalRandom.current().nextInt(0,3);
        return name[rand];
    }
}