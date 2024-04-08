import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.net.DatagramPacket;


public class Device {
    protected String name;
    protected String ip;
    protected int port;

//    JSONObject jsonData = Parser.parseJSONFile("src/NetworkConfig.json");

    Device(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    protected String getName() {
        return this.name;
    }


    public String getIp() {
        return this.ip;
    }

    public int getPort() {return this.port;}

    public void constructUDPacket(String destinationIP, int destinationPort, Map<String, VectorEntry> payload) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(payload);
            byte[] sendData = baos.toByteArray();
            oos.close();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destinationIP), Math.toIntExact(destinationPort));
            socket.send(packet);
            System.out.println("Packet sent to " + destinationIP + ":" + destinationPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}