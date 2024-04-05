import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class Router extends Device  {
    private Map<String, VectorEntry> distanceVector;
    private Map<String, Map<String, VectorEntry>> neighbors;
    private Map<String, String> nextHop;
    private final List<String> subnets;
    JSONObject jsonData = Parser.parseJSONFile("src/RouterConfig.json");
    Parser parser = new Parser();

    public Router(String name, String ip, int port) {
        super(name, ip, port);
        this.distanceVector = new HashMap<>();
        this.nextHop = new HashMap<>();
        this.neighbors = new HashMap<>();
        this.subnets = Parser.getSubnets(jsonData, this.name);
        initializeNeighbors(this.name);
        initDistanceVector();

//        this.ip = subnet + ".1"; // Assuming router's IP address is subnet.1
    }

    // Method to add a neighbor and its distance vector
    public void addNeighbor(String neighborName, Map<String, VectorEntry> neighborVector) {
        neighbors.put(neighborName, neighborVector);
    }

    private void initializeNeighbors(String routerName){
        List<String> neighborList = parser.getNeighbors(routerName, jsonData);
        for(String neighborName : neighborList) {
            neighbors.put(neighborName, new HashMap<>());
        }
    }

    private void initDistanceVector() {
        for(String node : subnets) {
            String subnet = node;
            VectorEntry entry = new VectorEntry(subnet, 0, this.name);
            distanceVector.put(subnet, entry);
            System.out.println(distanceVector);

            for (String key : distanceVector.keySet()) {
                if (key.equals(subnet)) {
                    VectorEntry item = distanceVector.get(key);
                    System.out.printf("Router %s 's  initial distance vector \n", this.name);
                    System.out.println(item.getName() + " " + item.getCost() + " " + item.getNextHop());
                }
            }
        }
    }


    // Method to update the distance vector based on received vectors from neighbors
    public void updateDistanceVector() {
        for (String destination : distanceVector.keySet()) {
            if (!destination.equals(name)) {
                int minDistance = Integer.MAX_VALUE;
                String minNextHop = destination; // Initially assume direct connection

                for (String neighbor : neighbors.keySet()) {
                    VectorEntry neighborEntry = neighbors.get(neighbor).get(destination);
                    if(neighborEntry != null) {
                        int distance = neighborEntry.getCost();
                        distance += distanceVector.get(neighbor).getCost();

                        if(distance < minDistance) {
                            minDistance = distance;
                            minNextHop = neighbor;
                        }
                    }
                }

                if (minDistance < distanceVector.get(destination).getCost()) {
                    distanceVector.put(destination, new VectorEntry(destination, minDistance, minNextHop));
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
//                updateDistanceVector();
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
        new Router("R1", "192.168.1", 3000);
        new Router("R2", "192.168.1", 3001);
//        Router routerA = new Router("A", "192.168.1", 3000);
//        Router routerB = new Router("B", "192.168.1", 3000);
//
//        Map<String, VectorEntry> initalDVectorA = new HashMap<>();
//        VectorEntry v1 = new VectorEntry("N1", 0, routerA.getName());
//        VectorEntry v2 = new VectorEntry("N2", 0, routerB.getName());
//        Map<String, VectorEntry> initalDVectorB = new HashMap<>();
//        VectorEntry v3 = new VectorEntry("N2", 0, routerA.getName());
//        VectorEntry v4 = new VectorEntry("N3", 0, routerB.getName());
//
//        initalDVectorA.put("N1", v1);
//        initalDVectorA.put("N2", v2);
//
//        initalDVectorB.put("N2", v3);
//        initalDVectorB.put("N3", v4);
//
//
//
//        // Simulate neighbor routers with their distance vectors
//        Map<String, VectorEntry> neighborVectorA = new HashMap<>();
//        VectorEntry vecEntry1 = new VectorEntry("N4", 0, "B");
//        VectorEntry vecEntry2 = new VectorEntry("N5", 0, "B");
//        neighborVectorA.put("N4",  vecEntry1);
//        neighborVectorA.put("N5", vecEntry2);
//        routerA.addNeighbor("B", neighborVectorA);
//
//        Map<String, VectorEntry> neighborVectorB = new HashMap<>();
//        VectorEntry vecEntry3 = new VectorEntry("N5", 0, "A");
//        VectorEntry vecEntry4 = new VectorEntry("N6", 0, "A");
//        neighborVectorB.put("N5", vecEntry3);
//        neighborVectorB.put("N6", vecEntry4);
//        routerB.addNeighbor("A", neighborVectorB);
//
//        // Initialize A and start distance vector protocol
////        routerA.setDistanceVector(initalDVectorA);
////        routerA.updateDistanceVector();
////        routerA.startDistanceVectorProtocol(5000); // Update every 5 seconds
//
//        // Initialize B and start distance vector protocol
////        routerB.setDistanceVector(initalDVectorB);
////        routerB.updateDistanceVector();
////        routerB.startDistanceVectorProtocol(5000); // Update every 5 seconds
    }
}

