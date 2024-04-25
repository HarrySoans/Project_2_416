import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.*;
import java.util.*;

public class Router extends Device {

    String ip;
    int port;
    DistanceVector distanceVector;
    private Map<String, Map<String, VectorEntry>> neighbors;
    private Map<String, String> nextHop;
    private final List<String> subnets;
    JSONObject jsonData = Parser.parseJSONFile("src/RouterConfig2.json");
    Parser parser = new Parser();


    public Router(String name) {
        super(name);
        this.ip = parser.getIpByName(name, jsonData);
        this.port = parser.getPortByName(name, jsonData);
        this.distanceVector = new DistanceVector(this.name, new HashMap<>());
        this.nextHop = new HashMap<>();
        this.neighbors = new HashMap<>();
        this.subnets = Parser.getSubnets(jsonData, this.name);
        initializeNeighbors(this.name);
        initDistanceVector();
        sendDistanceVectorToNeighbors();
        startDistanceVectorProtocol(5000);
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
            distanceVector.addEntry(subnet, entry);
        }
    }

    private void sendDistanceVectorToNeighbors() {
        for (String neighbor : neighbors.keySet()) {
            String ip = parser.getIpByName(neighbor, jsonData);
            int port = parser.getPortByName(neighbor, jsonData);
            constructUDPacket(ip, port, distanceVector);
        }
    }

    private DatagramPacket packetReceiver() {
        DatagramPacket finalMess = null;
        System.out.println("waiting...");
        try {
            DatagramSocket socket = new DatagramSocket(this.port);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            finalMess = receivePacket;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return finalMess;
    }

    public void constructUDPacket(String destinationIP, int destinationPort, DistanceVector payload) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(destinationIP);
            String stringified = payload.toString();
            byte[] sendData = stringified.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, destinationPort);
            socket.send(sendPacket);
            socket.close();
            System.out.println("packet sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to update the distance vector based on received vectors from neighbors
    public boolean updateDistanceVector(DistanceVector incomingDistanceVectors) {
        boolean isUpdated = false;
        for (VectorEntry entry : incomingDistanceVectors.getDV().values()) {
            String subnet = entry.getName();
            int cost = entry.getCost();
            Map<String, VectorEntry> dv = distanceVector.getDV();

            if (!dv.containsKey(subnet)) {
                distanceVector.addEntry(subnet, new VectorEntry(subnet, cost + 1, incomingDistanceVectors.getSenderName()));
                isUpdated = true;
            }else {
                // check shorter distance
                if((cost + 1) < distanceVector.getDV().get(subnet).cost) {
                    distanceVector.addEntry(subnet, new VectorEntry(subnet, cost + 1, incomingDistanceVectors.getSenderName()));
                    isUpdated = true;
                }
            }
        }
        return isUpdated;
    }


    protected DistanceVector receivePacket(DatagramPacket packet) {
        DistanceVector receivedVector = null;
        try {
            byte[] receivedData = packet.getData();
            String dv = new String(receivedData, 0, packet.getLength());
            String[] lines = dv.split("\n");
            String senderName = lines[0].substring(lines[0].indexOf(":") + 1).trim();
            Map<String, VectorEntry> distanceVector = new HashMap<>();
            for (int i = 2; i < lines.length; i++) {
                String[] parts = lines[i].trim().split(":");
                String subnet = parts[1].trim().split(",")[0].trim();
                VectorEntry entry = VectorEntry.parseVectorEntry(parts[2].trim());
                distanceVector.put(subnet, entry);
            }
            receivedVector = new DistanceVector(senderName, distanceVector);

            System.out.println(receivedVector.senderName);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivedVector;
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
        for (String destination : distanceVector.getDV().keySet()) {
            VectorEntry dv = distanceVector.getDV().get(destination);
            String nexHop = dv.getNextHop();
            String cost = String.valueOf(dv.getCost());


            System.out.println("Destination: " + destination + ", Next Hop: " + nexHop +
                    ", Distance: " + cost);
        }
        System.out.println();
    }

    // Example main method for testing
    public static void main(String[] args) throws IOException {
        Router r1 = new Router(args[0]);

        while(true) {
            DatagramPacket packet = r1.packetReceiver();
            DistanceVector newDV = r1.receivePacket(packet);
            boolean isUpdated = r1.updateDistanceVector(newDV);
            System.out.println(newDV);
            r1.startDistanceVectorProtocol(3000);
            if(isUpdated) {
                r1.sendDistanceVectorToNeighbors();
            }
        }
    }
}

