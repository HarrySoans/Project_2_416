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

public class Router extends Device implements Runnable  {
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
        }
    }

    private void sendDistanceVectorToNeighbors() {
        for(String neighbor : neighbors.keySet()) {
            String ip = parser.getIpByName(neighbor, jsonData);
            int port = parser.getPortByName(neighbor, jsonData);
            constructUDPacket(ip, port, this.distanceVector);
            System.out.println("neigh: " + neighbor + "\n" + "ip: " + ip + "\n" + "port: " + port);
        }
    }


    // Method to update the distance vector based on received vectors from neighbors
    public void updateDistanceVector () {
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
            System.out.println("Destination: " + destination + ", Next Hop: " + nextHop.get(destination) +
                    ", Distance: " + distanceVector.get(destination));
        }
        System.out.println();
    }

    // Example main method for testing
    public static void main(String[] args) throws SocketException {
//        Router r1 = new Router("R1", "192.168.1", 3000);
//        Thread routerThread = new Thread(r1);
//        routerThread.start();
        Router r2 = new Router("R2", "192.100.4.1", 3001);
        Thread routerThread2 = new Thread(r2);
        routerThread2.start();

//        new Router("R2", "192.168.1", 3001);
    }
}

