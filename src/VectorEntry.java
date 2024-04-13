import java.io.Serializable;
import java.util.Map;

public class VectorEntry implements Serializable {
    //contains name, distance, next hop
    String name;
    int cost;
    String nextHop;

    VectorEntry(String name, int cost, String nextHop) {
        this.name = name;
        this.cost = cost;
        this.nextHop = nextHop;
    }

    public String getName() {return this.name;}

    public int getCost() {return this.cost;}

    public String getNextHop() {return this.nextHop;}


    public String toStringEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("/").append(cost).append("/").append(nextHop);
        return sb.toString();
    }
    public static VectorEntry parseVectorEntry(String entryString) {
        String[] parts = entryString.split("/");
        String name = parts[0].trim();
        int cost = Integer.parseInt(parts[1].trim());
        String nextHop = parts[2].trim();
        return new VectorEntry(name, cost, nextHop);
    }
}
