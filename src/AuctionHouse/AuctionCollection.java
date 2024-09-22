package AuctionHouse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author
 * Auction Collection, act as a simple database, with timer for bidding activity
 */
public class AuctionCollection {
    JSONArray collection;
    String[] randomName = {"item1", "item1", "item1", "item1",
            "item1",
            "item1",
            "item1", "item1", "item1", "item1", "item1", "item1"};
    private int ID = -1;
    private final ArrayList<Timer> timers = new ArrayList<>();

    /**
     * Constructor, create a random list of items (in term of json object)
     * @throws JSONException Exception when creating json
     */
    public AuctionCollection() throws JSONException {
        this.collection = new JSONArray();
    }

    /**
     * Add item to collection
     * @param json item in term of json object
     */
    public void addItem(JSONObject json) {
        collection.put(json);
        timers.add(new Timer());
    }

    /**
     * Check if bids are being processed
     * @return true if it is, false otherwise
     * @throws JSONException Exception when creating json
     */
    public boolean isTimerRunning() throws JSONException {
        for (int i = 0; i < collection.length(); i++) {
            JSONObject json = collection.getJSONObject(i);
            if (json.getString("State").equals("BIDDING")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get next random name
     * @return random name
     */
    private String nextName() {
        return randomName[ThreadLocalRandom.current().nextInt(randomName.length-1)];
    }

    /**
     * Set timer for an item for 30 seconds, cancel and reset if new high bid in
     * @param id item ID
     * @param task announce winner after 30 seconds of highest bid
     */
    synchronized public boolean setTimerAt(int id, TimerTask task) throws JSONException {
        int index = -1;
        for (int i = 0; i < collection.length(); i++) {
            if (collection.getJSONObject(i).getInt("ItemID") == id) {
                collection.getJSONObject(i).remove("State");
                collection.getJSONObject(i).put("State", "BIDDING");
                index = i;
                break;
            }
        }

        if (index == -1) { return false; }

        timers.get(index).cancel();
        Timer timer = new Timer();
        timers.set(index, timer);
        timer.schedule(task, 30000);
        return true;
    }

    /**
     * Get collection
     * @return collection in term of json object
     */
    public JSONArray getCollection() {
        return collection;
    }

    /**
     * Get item with given id
     * @param id item id
     * @return return item in term of json object
     * @throws JSONException exception when getting json object
     */
    public JSONObject getItem(int id) throws JSONException {
        for (int i = 0; i < collection.length(); i++) {
            if (collection.getJSONObject(i).getInt("ItemID") == id) {
                return collection.getJSONObject(i);
            }
        }
        return null;
    }

    /**
     * Check if all items are sold
     * @return true if it is, false otherwise
     * @throws JSONException Exception when creating json
     */
    public boolean isAllSold() throws JSONException {
        for (int i = 0; i < collection.length(); i++) {
            JSONObject json = collection.getJSONObject(i);
            if (json.getString("State").matches("AVAILABLE|BIDDING")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get a nice format of collection in String
     * @return String of list of Available items
     */
    public String toString() {
        String list = "";
        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject json = collection.getJSONObject(i);
                if (json.getString("State").matches("AVAILABLE|BIDDING")) {
                    list += "ItemID " + json.getInt("ItemID") + " : ";
                    list += json.getString("Name") + " : ";
                    list += "$" + json.getInt("CurrentBid") + " initial price ";
                    list += "$" + json.getInt("InitialPrice") + ",";
                }
            }
        } catch (JSONException e) {
            System.err.println("toString ERROR " + e);
        }
        return list;
    }

    /**
     * To String of all items including sold items
     * @return String of list of all items
     */
    public String fullDetails() {
        String list = "";
        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject json = collection.getJSONObject(i);
                list += "<" + json.getString("State") + "> ";
                list += "ItemID " + json.getInt("ItemID") + " : ";
                list += json.getString("Name") + " : ";
                list += "$" + json.getInt("CurrentBid") + " initial price ";
                list += "$" + json.getInt("InitialPrice") + ",";
            }
        } catch (JSONException e) {
            System.err.println("toString ERROR " + e);
        }
        return list;
    }
}




