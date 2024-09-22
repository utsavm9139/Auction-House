package AuctionHouse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * House client, interact with the bank
 */
public class HouseClient implements Runnable{
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int accountID;
    private int my_portNumber;

    /**
     * Constructor, establish connection with host name and port number
     * @param hostName server host name
     * @param host_portNumber server port number
     * @param my_portNumber house current server's port number
     */
    public HouseClient(String hostName, int host_portNumber, int my_portNumber) {
        this.my_portNumber = my_portNumber;
        try {
            socket = new Socket(hostName, host_portNumber);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connecting to the bank...");
            TimeUnit.SECONDS.sleep(1);
            System.out.println("Successfully connected.");

        }  catch (IOException e) {
            System.err.println("Exception when connecting to the bank: " + e);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Exception when waiting: " + e);
            System.exit(1);
        }

    }

    /**
     * Get connection socket
     * @return connection socket with the bank
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Getter for accountID
     * @return account ID
     */
    public int getAccountID() {
        return accountID;
    }

    /**
     * Send a request to open a bank account then listening to the bank
     */
    @Override
    public void run() {
        try {
            JSONObject json = new JSONObject();
            json.put("SenderType", "AUCTION");
            json.put("MessageType","AUCTIONINIT");
            json.put("ExtraInfo", InetAddress.getLocalHost().getHostName() + " " + my_portNumber);

            out.println(json.toString());

            String line = in.readLine();
            System.out.println(line);

            String[] inputLine = line.split(" ");
            accountID = Integer.parseInt(inputLine[3]);

            while (true) {

            }
        } catch (JSONException e) {
            System.err.println("Exception when create json: " + e);
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }
}