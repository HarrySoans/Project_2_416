import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.ls.LSOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class Router extends Device implements Runnable {
    private Map<String, VectorEntry> distanceVector;
    private Map<String, Map<String, VectorEntry>> neighbors;
    private Map<String, String> nextHop;
    private final List<String> subnets;
    JSONObject jsonData = Parser.parseJSONFile("src/RouterConfig.json");
    Parser parser = new Parser();
    private final DatagramSocket socket;

    public Router(String name, String ip, int port) throws SocketException {
        super(name, ip, port);
        this.distanceVector = new HashMap<>();
        this.nextHop = new HashMap<>();
        this.neighbors = new HashMap<>();
        this.subnets = Parser.getSubnets(jsonData, this.name);
        this.socket = new DatagramSocket(port);
        initializeNeighbors(this.name);
        initDistanceVector();
    }

    public void run() {
        sendDistanceVectorToNeighbors();
        startDistanceVectorProtocol(5000);
        receivePackets();
    }

    // Method to add a neighbor and its distance vector
    public void addNeighbor(String neighborName, Map<String, VectorEntry> neighborVector) {
        neighbors.put(neighborName, neighborVector);
    }

    private void initializeNeighbors(String routerName) {
        List<String> neighborList = parser.getNeighbors(routerName, jsonData);
        for (String neighborName : neighborList) {
            neighbors.put(neighborName, new HashMap<>());
        }
    }

    private void initDistanceVector() {
        for (String node : subnets) {
            String subnet = node;
            VectorEntry entry = new VectorEntry(subnet, 0, this.name);
            distanceVector.put(subnet, entry);
        }
    }

    private void sendDistanceVectorToNeighbors() {
        for (String neighbor : neighbors.keySet()) {
            String ip = parser.getIpByName(neighbor, jsonData);
            int port = parser.getPortByName(neighbor, jsonData);
            constructUDPacket(ip, port, this.distanceVector);
            System.out.println("neighbor: " + neighbor + "\n" + "ip: " + ip + "\n" + "port: " + port);
        }
    }


    // Method to update the distance vector based on received vectors from neighbors
    public void updateDistanceVector(Map<String, Map<String, VectorEntry>> incomingDistanceVectors) {
        for (String neighbor : incomingDistanceVectors.keySet()) {
            if (neighbors.containsKey(neighbor)) {
                for (Map<String, VectorEntry> entryMap : incomingDistanceVectors.values()) {
                    for (VectorEntry entry : entryMap.values()) {
                        String subnet = entry.getName();
                        int cost = entry.getCost();

                        if (!distanceVector.containsKey(subnet)) {
                            distanceVector.put(subnet, new VectorEntry(subnet, cost + 1, neighbor));
                        }
                    }
                }
            }
        }
    }


    private void receivePackets() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                socket.receive(packet);
                receivePacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void receivePacket(DatagramPacket packet) {
        try {
            byte[] receivedData = packet.getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(receivedData);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Map<String, VectorEntry> receivedVector = (Map<String, VectorEntry>) ois.readObject();
            ois.close();
            System.out.printf("Received: %s", receivedVector);
        } catch (Exception e) {
            // Handle any exceptions that occur during frame deserialization or processing
            e.printStackTrace();
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
            VectorEntry dv = distanceVector.get(destination);
            String nexHop = dv.getNextHop();
            String cost = String.valueOf(dv.getCost());


            System.out.println("Destination: " + destination + ", Next Hop: " + nexHop +
                    ", Distance: " + cost);
        }
        System.out.println();
    }

    // Example main method for testing
    public static void main(String[] args) throws SocketException {
        Router r1 = new Router("R1", "192.168.1", 3000);
        Thread routerThread = new Thread(r1);
        routerThread.start();
//        Router r2 = new Router("R2", "192.100.4.1", 3001);
//        Thread routerThread2 = new Thread(r2);
//        routerThread2.start();

        Map<String, Map<String, VectorEntry>> incomingDistanceVectors = new HashMap<>();

        Map<String, VectorEntry> neighbor1DistanceVector = new HashMap<>();
        neighbor1DistanceVector.put("N3", new VectorEntry("N3", 0, "R2")); // Example entry
        neighbor1DistanceVector.put("N4", new VectorEntry("N4", 0, "R2")); // Example entry

        incomingDistanceVectors.put("R2", neighbor1DistanceVector);

        r1.updateDistanceVector(incomingDistanceVectors);

        System.out.println(r1.distanceVector);


//        for (Map.Entry<String, Map<String, VectorEntry>> entry : incomingDistanceVectors.entrySet()) {
//            System.out.println("Router: " + entry.getKey());
//            Map<String, VectorEntry> distanceVector = entry.getValue();
//            for (Map.Entry<String, VectorEntry> vectorEntry : distanceVector.entrySet()) {
//                System.out.println("Destination: " + vectorEntry.getKey() +
//                        ", Cost: " + vectorEntry.getValue().getCost() +
//                        ", Next Hop: " + vectorEntry.getValue().getNextHop());
//            }
//            System.out.println();
//        }


//        new Router("R2", "192.168.1", 3001);
    }
}

