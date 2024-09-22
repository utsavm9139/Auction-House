package bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author
 * Bank Server, keep the Bank running, and accepting clients
 */
public class BankServer {
    /**
     * Main class, where running the bank
     * @param args input args to start the bank server
     */
    public static void main(String[] args) {
        Bank bank = new Bank();
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket serverSocket = null;
        int accountID = 0;
        ArrayList<String> auctionInfo = new ArrayList<>();
        ArrayList<Socket> auctionList = new ArrayList<>();
        ArrayList<Socket> agentList = new ArrayList<>();
        System.out.println("Listening on port: " + portNumber);

        try {
            serverSocket = new ServerSocket(portNumber);
        }
        catch (IOException e) {
            System.out.println("Error when listening on port " + portNumber);
            System.exit(1);
        }

        while (true) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("A new client just connected.");

                BankLogic bl = new BankLogic(bank, client, accountID++, auctionInfo, auctionList, agentList);
                Thread t = new Thread(bl);
                t.start();
            }
            catch (IOException e) {
                System.out.println("Error when accepting client");
            }
        }
    }
}