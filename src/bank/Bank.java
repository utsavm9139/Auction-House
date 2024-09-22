package bank;
import java.util.HashMap;

/**
 * @author
 * Bank class, act as a Bank and deal with request action about accounts
 */
public class Bank {
    private final HashMap<Integer, BankAccount> accountsDB;

    /**
     * Constructor, create a bank
     */
    public Bank()  {
        accountsDB = new HashMap<>();
    }

    /**
     * Open an account with given balance
     * @param id bank account ID
     * @param name name of owner
     * @param balance given amount
     * @return Message with account number
     */
    public String openAccount(int id, String name, double balance) {
        if (balance < 0) {
            balance = 0;
        }
        accountsDB.put(id, new BankAccount(name, balance));
        return "Your account# is " + id;
    }

    /**
     * Check balance, available fund, and holding
     * @param id bank account ID
     * @return statement of balance
     */
    public String checkFunds(int id) {
        BankAccount account = accountsDB.get(id);
        String s = "Total balance: " + account.getBalance() + ",";
        s += "Available funds: " + account.getAvailableFunds() + ",";
        s += "Holding funds: " + account.getHoldFunds();
        return s;
    }

    /**
     * Block a funds in a given account
     * @param id bank account ID
     * @param amount blocking amount
     * @return Result of request (Successful/Unsuccessful)
     */
    public String blockFunds(int id, double amount) {
        if (amount <= 0) return "Unsuccessful";
        BankAccount account = accountsDB.get(id);
        return account.blockFunds(amount) ? "Successful" : "Unsuccessful";
    }

    /**
     * Unblock a fund in a given account
     * @param id bank account ID
     * @param amount amount needed to be unblocked
     * @return Result of request (Successful/Unsuccessful)
     */
    public String unblockFunds(int id, double amount) {
        if (amount <= 0) return "Unsuccessful";
        BankAccount account = accountsDB.get(id);
        return account.unblockFunds(amount) ? "Successful" : "Unsuccessful";
    }

    /**
     * Transfer fund from one account to another
     * @param fromID from account ID
     * @param toID to account ID
     * @param amount transferring amount
     * @return Result of request (Successful/Unsuccessful)
     */
    public String transferFunds(int fromID, int toID, double amount) {
        if (amount <= 0) return "Unsuccessful";
        BankAccount fromAccount = accountsDB.get(fromID);
        BankAccount toAccount = accountsDB.get(toID);
        return fromAccount.transferFundTo(toAccount, amount) ? "Successful" : "Unsuccessful";
    }
}