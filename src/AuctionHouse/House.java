package AuctionHouse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author
 * House class, where starts server and register with the bank
 */
public class House {
    /**
     * Main class, get input form user, to start the house and set up connections
     * @param args input args
     * @throws JSONException Exception when creating json
     * @throws InterruptedException Exception when sleep the thread
     */
    public static void main(String[] args) throws JSONException, InterruptedException {
        int house_portNumber = Integer.parseInt(args[0]);
        String bank_hostName = args[1];
        int bank_portNumber = Integer.parseInt(args[2]);
        String dataServer_hostName = args[3];
        int dataServer_portNumber = Integer.parseInt(args[4]);
        int numItem = Integer.parseInt(args[5]);
        String inputLine = null;

        try {
            inputLine = sendRequest(dataServer_hostName, dataServer_portNumber,
                    "GETITEMS " + numItem);
        }
        catch (IOException e) {
            System.err.println("Cannot connect to Data Server");
            System.exit(1);
        }

        // Convert item into json for easier use
        AuctionCollection collection = new AuctionCollection();

        JSONArray jsonArray = new JSONArray(inputLine);

        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.get(i) instanceof JSONObject) {
                JSONObject json = jsonArray.getJSONObject(i);
                int id = json.getInt("ItemID");
                String name = json.getString("name");
                double price = json.getDouble("price");

                json = new JSONObject();
                json.put("ItemID", id);
                json.put("Name", name);
                json.put("CurrentBid", price);
                json.put("InitialPrice", price);
                json.put("State", "AVAILABLE");
                json.put("Winner", -1);
                json.put("AccountID", -1);
                collection.addItem(json);
            }
        }

        // Connecting to bank
        HouseClient hc = new HouseClient(bank_hostName, bank_portNumber, house_portNumber);
        Thread t1 = new Thread(hc);
        t1.start();

        // Wait a second then creating server for bidding
        TimeUnit.SECONDS.sleep(1);
        HouseServer hs = new HouseServer(house_portNumber, collection, hc);
        Thread t2 = new Thread(hs);
        t2.start();

        // Listener for terminating request from user
        System.out.println("Type X to close the auction house. It will only " +
                "close if no bids are being processed\n");
        UserListener userListener = new UserListener(hc.getSocket(),collection);
        Thread t3 = new Thread(userListener);
        t3.start();
    }

    /**
     * Send request in term of json string
     * @param hostName host name
     * @param portNumber port number
     * @param request json string
     * @return input stream answer from the server
     * @throws IOException
     */
    private static String sendRequest(String hostName, int portNumber, String request) throws IOException {
        Socket socket = new Socket(hostName,portNumber);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Requesting items from Data Server...");
        out.println(request);
        String input = in.readLine();
        System.out.println("Item received.");
        socket.close();
        return input;
    }

    /**
     * Sub class, listener to terminating request from user
     */
    private static class UserListener implements Runnable {
        private final Socket socket;
        private final AuctionCollection collection;

        /**
         * Constructor, get the connection and database of collection
         * @param socket connection with the bank
         * @param collection collection database
         */
        public UserListener(Socket socket, AuctionCollection collection) throws JSONException {
            this.socket = socket;
            this.collection = collection;
        }

        /**
         * Listening to input from user, can only close if no bidding activity
         */
        @Override
        public void run() {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    String inputLine = stdIn.readLine();
                    if (inputLine.equalsIgnoreCase("X")) {
                        if (!(collection.isTimerRunning())) {
                            System.exit(1);
                        }
                        else {
                            System.out.println("There are bids being processed.");
                        }
                    }
                    else if (inputLine.equalsIgnoreCase("CHECKDETAILS")) {
                        Stream.of(collection.fullDetails().split(","))
                                .forEach(System.out::println);
                    }
                    else {
                        System.out.println("Invalid Command");
                        System.out.println("Type X to close the auction house." +
                                " It will only close if no bids are being processed");
                    }
                } catch (IOException | JSONException e) {
                    System.out.println("Cannot read input from User");
                }
            }
        }
    }
}