package AuctionHouse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author
 * House server, keep auction house running and accepting agents
 */
public class HouseServer implements Runnable{
    private final ServerSocket serverSocket;
    private final AuctionCollection collection;
    private final ArrayList<Socket> agentList;
    private int clientID;
    private final HouseClient houseClient;

    /**
     * Constructor, set up server
     * @param portNumber auction house port number
     * @param collection collection database
     * @param houseClient connection between house and the bank
     */
    public HouseServer(int portNumber, AuctionCollection collection, HouseClient houseClient) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening on : " + InetAddress.getLocalHost().getHostName() + ":" + portNumber);
        }
        catch (IOException e) {
            System.out.println("Error when listening on port " + portNumber);
            System.exit(1);
        }
        this.serverSocket = serverSocket;
        this.collection = collection;
        this.agentList = new ArrayList<>();
        this.clientID = 0;
        this.houseClient = houseClient;
    }

    /**
     * Accept client, give an ID, save the connection to the list
     * Then let House Logic deal with them
     */
    @Override
    public void run() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("An agent just connected");

                HouseLogic houseLogic = new HouseLogic(client, clientID, collection, agentList, houseClient);
                clientID++;
                agentList.add(client);
                Thread t = new Thread(houseLogic);
                t.start();
            } catch (IOException e) {
                System.out.println("Error when accepting client");
            }
        }
    }
}