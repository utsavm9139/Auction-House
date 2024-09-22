package bank;

import java.util.HashMap;

/**
 * @author
 * Bank Account class, include name, balance, available fund, and holding
 */
public class BankAccount {
    private String name;
    private double balance;
    private double availableFunds;
    private double holdFunds;
    private HashMap<Integer, Double> invoice;

    /**
     * Constructor, initialize name and amount
     * @param name name
     * @param amount first time deposit amount
     */
    public BankAccount(String name, double amount) {
        this.name = name;
        this.balance = amount;
        this.availableFunds = balance;
        this.invoice = new HashMap<>();
    }

    /**
     * Deposit fund
     * @param amount amount
     * @return true if successful, false otherwise
     */
    public boolean addFunds(double amount) {
        if (amount <= 0) return false;
        this.balance += amount;
        return true;
    }

    /**
     * Transfer fund from this account to a given account ID
     * @param account given account ID
     * @param amount transferring fund
     * @return true if successful, false otherwise
     */
    public boolean transferFundTo(BankAccount account, double amount) {
        if (amount <= 0) { return false; }
        if (holdFunds >= amount) {
            holdFunds -= amount;
            account.addFunds(amount);
            balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Block fund
     * @param amount blocking amount
     * @return true if successful, false otherwise
     */
    public boolean blockFunds(double amount) {
        if (amount <= 0) { return false; }
        if (amount > availableFunds) { return false; }
        holdFunds += amount;
        availableFunds = balance - holdFunds;
        return true;
    }

    /**
     * Unblock fund
     * @param amount unblocking amount
     * @return true if successful, false otherwise
     */
    public boolean unblockFunds(double amount) {
        if (amount <= 0) { return false; }
        if (holdFunds < amount) return false;
        holdFunds -= amount;
        availableFunds += amount;
        return true;
    }

    /**
     * Get balance
     * @return balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Get available fund
     * @return available fund
     */
    public double getAvailableFunds() {
        return availableFunds;
    }

    /**
     * Get holding
     * @return holding fund
     */
    public double getHoldFunds() {
        return holdFunds;
    }
}