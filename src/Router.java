import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Router extends Device {
    private Map<String, Integer> distanceVector;
    private Map<String, String> nextHop;
    private Map<String, Map<String, Integer>> neighbors;

    public Router(String name, String subnet) {
        super(name);
        this.distanceVector = new HashMap<>();
        this.nextHop = new HashMap<>();
        this.neighbors = new HashMap<>();
        this.ip = subnet + ".1"; // Assuming router's IP address is subnet.1
    }

    // Method to add a neighbor and its distance vector
    public void addNeighbor(String neighborName, Map<String, Integer> neighborVector) {
        neighbors.put(neighborName, neighborVector);
    }

    // Method to initialize the distance vector with the router's directly connected neighbors
    public void initializeDistanceVector() {
        for (String neighbor : neighbors.keySet()) {
            distanceVector.put(neighbor, neighbors.get(neighbor).getOrDefault(name, Integer.MAX_VALUE));
            nextHop.put(neighbor, neighbor);
        }
        distanceVector.put(name, 0); // Distance to self is 0
    }

    // Method to update the distance vector based on received vectors from neighbors
    public void updateDistanceVector() {
        for (String destination : distanceVector.keySet()) {
            if (!destination.equals(name)) {
                int minDistance = Integer.MAX_VALUE;
                String minNextHop = destination; // Initially assume direct connection

                for (String neighbor : neighbors.keySet()) {
                    int distance = neighbors.get(neighbor).getOrDefault(destination, Integer.MAX_VALUE);
                    distance += distanceVector.get(neighbor); // Add distance to neighbor

                    if (distance < minDistance) {
                        minDistance = distance;
                        minNextHop = neighbor;
                    }
                }

                if (minDistance < distanceVector.get(destination)) {
                    distanceVector.put(destination, minDistance);
                    nextHop.put(destination, minNextHop);
                }
            }
        }
    }

    // Method to periodically update the routing table using the distance vector protocol
    public void startDistanceVectorProtocol(long interval) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDistanceVector();
                System.out.println("Routing table updated for Router " + name);
                printRoutingTable();
            }
        }, interval, interval);
    }

    // Method to print the routing table
    public void printRoutingTable() {
        System.out.println("Routing table for Router " + name + ":");
        for (String destination : distanceVector.keySet()) {
            System.out.println("Destination: " + destination + ", Next Hop: " + nextHop.get(destination) +
                    ", Distance: " + distanceVector.get(destination));
        }
        System.out.println();
    }

    // Example main method for testing
    public static void main(String[] args) {
        Router routerA = new Router("A", "192.168.1");
        Router routerB = new Router("B", "192.168.1");

        // Simulate neighbor routers with their distance vectors
        Map<String, Integer> neighborVectorA = new HashMap<>();
        neighborVectorA.put("A", 0);
        neighborVectorA.put("B", 1);
        routerA.addNeighbor("B", neighborVectorA);

        Map<String, Integer> neighborVectorB = new HashMap<>();
        neighborVectorB.put("A", 1);
        neighborVectorB.put("B", 0);
        routerB.addNeighbor("A", neighborVectorB);

        // Initialize A and start distance vector protocol
        routerA.initializeDistanceVector();
        routerA.startDistanceVectorProtocol(5000); // Update every 5 seconds

        // Initialize B and start distance vector protocol
        routerB.initializeDistanceVector();
        routerB.startDistanceVectorProtocol(5000); // Update every 5 seconds
    }
}

