import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser {
    static List<String> neighbors = new ArrayList<>();

    public static JSONObject parseJSONFile(String filename) {
        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader(filename);
            return (JSONObject) parser.parse(reader);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getSubnets(JSONObject data, String routerName) {
        List<String> subnets = new ArrayList<>();
        JSONArray arr = (JSONArray) data.get("subnet");
        for(Object ob:arr) {
            JSONObject subnetObj = (JSONObject) ob;
            for(Object key : subnetObj.keySet()) {
                if(key.equals(routerName)) {
                    JSONArray subnetsList = (JSONArray) subnetObj.get(routerName);
                    System.out.println(subnetsList);
                    for (Object o : subnetsList) {
                        String node = (String) o;
                        subnets.add(node);
                    }
                }
            }
        }
        return subnets;
    }

    public List<String> getNeighbors(String routerName, JSONObject data) {
        List<String> neighbors = new ArrayList<>();
        JSONArray arr = (JSONArray) data.get("neighbor");
        for(Object ob:arr) {
            JSONObject neighborObj = (JSONObject) ob;
            String node1 = (String) neighborObj.get("node1");
            String node2 = (String) neighborObj.get("node2");

            if (node1.equals(routerName)) {
                neighbors.add(node2);
            }
            if (node2.equals(routerName)) {
                neighbors.add(node1);
            }
        }
        return neighbors;
    }

    //Takes in a router name and retrieves IP
    public String getIpByName(String routerName, JSONObject data) {
        JSONArray arr = (JSONArray) data.get("routers");
        String ip = null;
        if(routerName != null) {
            for(Object ob:arr) {
                JSONObject routerObj = (JSONObject) ob;
                ip = (String) routerObj.get("ip");
            }
        }
        return ip;
    }

    //Takes in a router name and retrieves Port
//    public int getPortByName(String routerName) {
//
//    }


    public static void main(String[] args) {
        JSONObject jsonData = parseJSONFile("src/RouterConfig.json");
        getSubnets(jsonData, "R1");
//        if(jsonData != null) {
//            initRouters(jsonData);
//            deriveSubnetsAndGenerateVectorEntries(jsonData);
//        }else {
//            System.out.println("Failed to parse file");
//        }
    }
}
