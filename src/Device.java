import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
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

    public void constructUDPacket(String destinationIP, int destinationPort, String payload) {
        try {
            byte[] sendData = payload.getBytes();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destinationIP), Math.toIntExact(destinationPort));
            socket.send(packet);
            System.out.println("Packet sent: " + new String(packet.getData()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}