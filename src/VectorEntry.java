public class VectorEntry {
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
}
