package AGent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
 * class AgentBankLogic
 */
public class AgentBankLogic implements Runnable{
    private final BufferedReader in;
    private String[] inputLine;
    private final HashMap<String, Socket> socketList;
    private HashMap<Integer, Double> invoice = new HashMap<>();

    /**
     * constructor for AgentBankLogic
     * @param socket connect using socket
     * @param socketList a list of socket include key
     * @throws IOException exception
     */
    public AgentBankLogic(Socket socket, HashMap<String, Socket> socketList,
                          HashMap<Integer, Double> invoice)
            throws IOException {
        this.socketList = socketList;
        this.invoice = invoice;
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * function to get inputLine from bank
     * @return String array
     */
    public String[] getInputLine() {
        return inputLine;
    }

    /**
     * deal with input from the bank (bank send house id,
     * bank send balance,...)
     */
    @Override
    public void run() {
        try {
            do {
                //get input from bank
                String fromBank = in.readLine();

                if (!(fromBank == null)) {

                    //split into each line then put in array
                    inputLine = fromBank.split(",");
                    System.out.println();

                    //house list send from bank
                    if (inputLine[0].matches("House list")) {
                        if (!inputLine[1].matches("No houses available")) {
                            System.out.println("For your information, " +
                                    "here is an updated" +
                                    " list of all auction houses:");
                            for (int i = 1; i < inputLine.length; i++) {
                                if (!socketList.containsKey(inputLine[i])) {
                                    connectToHouse(inputLine[i], socketList);
                                }
                                System.out.println("House No." + i);
                            }
                        }
                    }
                    else if (inputLine[0].matches("Successful")) {
                        System.out.println("Thank you for your business.");
                        invoice.remove(Integer.parseInt(inputLine[1]));
                    }
                    //all other information from bank
                    else {
                        for (String line : inputLine) {
                            System.out.println(line);
                        }
                    }
                }
            } while (true);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * function connectToHouse
     * connect to any auction house that available
     */
    public static void connectToHouse(String auctionHouse, HashMap<String,
            Socket> socketList) {
        //String[] inputLine = auctionHouse.split(" ");
        //System.out.println(auctionHouse);
        //System.out.println("Connecting to house...");
        //System.out.println(inputLine[0] + "   " + inputLine[1]);
        socketList.put(auctionHouse, null);
        //System.out.println("Successful connect to house");
    }
}