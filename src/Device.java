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

}
