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

    Device(String name) {
        this.name = name;

    }

    protected String getName() {
        return this.name;
    }
}
