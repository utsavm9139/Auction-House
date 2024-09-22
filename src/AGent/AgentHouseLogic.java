package AGent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/**
 * Jiajun Guo
 * Steven Chen
 * Rajesh Upadhayaya
 *
 * CS 351L
 * Project 5: Distributed Auction
 *
 * class AgentHouseLogic
 */
public class AgentHouseLogic implements Runnable{
    private final HashMap<String, Socket> socketList;  private final BufferedReader in;
    private final String auctionHouse;
    private int isBidding;

    private HashMap<Integer, Double> invoice = new HashMap<>();

    /**
     * constructor for AgentHouseLogic
     * @param socket connect to house using socket
     * @param socketList a list of socket include key
     * @param auctionHouse auction house key
     * @param isBidding user is bidding
     * @throws IOException exception
     */
    public AgentHouseLogic (Socket socket, HashMap<String, Socket> socketList,
                            String auctionHouse, int isBidding,
                            HashMap<Integer, Double> invoice)
            throws IOException {
        this.socketList = socketList;
        this.auctionHouse = auctionHouse;
        this.isBidding = isBidding;
        this.invoice = invoice;
        //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * deal with input from house
     */
    @Override
    public void run() {
        try {
            do {
                //input from house
                String fromHouse = in.readLine();
                System.out.println();
                if (fromHouse == null) {
                    socketList.remove(auctionHouse);
                    System.out.println("House No." + (socketList.size() + 1) +
                            " is disconnect");
                    break;
                }
                else {
                    String[] inputLine = fromHouse.split(",");
                    if (inputLine[0].matches("Acceptance")) {
                        for (String line : inputLine) {
                            System.out.println(line);
                        }
                        isBidding++;
                    }
                    else if (inputLine[0].matches("Outbid")) {
                        for (String line : inputLine) {
                            System.out.println(line);
                        }
                        isBidding--;
                    }
                    else if (inputLine[0].matches("Winner")) {
                        System.out.println("Congratulation sir!!!");
                        System.out.print("You win the ");
                        System.out.println(inputLine[1]);

                        //get houseID and amount owe
                        int houseID = Integer.parseInt(inputLine[2]);
                        double amount = Double.parseDouble((inputLine[3]));

                        if (invoice.containsKey(houseID)) {
                            double tmpAmount = invoice.get(houseID);
                            invoice.replace(houseID, tmpAmount + amount);
                        }
                        else {
                            invoice.put(houseID, amount);
                        }

                        isBidding--;
                    }

                    else {
                        for (String item : inputLine) {
                            System.out.println(item);
                        }
                    }
                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * check if user is bidding any item
     * @return boolean
     */
    public boolean isBidding() {
        return (isBidding > 0);
    }

    /**
     * get invoice
     * @return invoice
     */
    /*public HashMap<Integer, Double> getInvoice() {
        return invoice;
    }
*/

}