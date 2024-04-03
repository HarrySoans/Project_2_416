import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {


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

    public static List<Router> initRouters(JSONObject data) {
        JSONArray routers = (JSONArray) data.get("routers");
        List<Router> computedRouters = new ArrayList<>();


        for(Object routerObj : routers) {
            try {
                JSONObject routerJSON = (JSONObject) routerObj;
                int port = ((Long) routerJSON.get("port")).intValue();
                String ip = (String) routerJSON.get("ip");
                String name = (String) routerJSON.get("name");

                Router router = new Router(name, ip, port);
                computedRouters.add(router);

             }catch (Exception e) {
                e.printStackTrace();
            }
        }

        //return a list of routers
        return computedRouters;
    }

    public static void deriveSubnetsAndGenerateVectorEntries(JSONObject data) {
        JSONArray subnets = (JSONArray) data.get("subnet");
        List<Router> routers = initRouters(data);
        Map<String, VectorEntry> distanceVector = null;

        for(Object subObj : subnets) {
            JSONObject subnetObj = (JSONObject) subObj;
            String routerName = (String) subnetObj.keySet().iterator().next();
            JSONArray nodes = (JSONArray) subnetObj.get(routerName);

            //for each router in our created routers, if the name is the same as the routerName then we want to generate entries inside of that routers distance vector containing each entry found in the array, each entry will be of type VectorEntry.
            Router router = routers.stream().filter(r -> r.getName().equals(routerName)).findFirst().orElse(null);

            if(router != null) {
                distanceVector = new HashMap<>();

                for(Object node : nodes) {
                    String subnet = (String) node;
                    VectorEntry entry = new VectorEntry(routerName, 0);
                    distanceVector.put(subnet, entry);
                }

                System.out.println("Generated distance vector: " + distanceVector);

                router.setDistanceVector(distanceVector);

            }else {
                return;
            }

            System.out.println("Router: " + routerName);
            System.out.println("Nodes: ");

            for(Object node : nodes) {
                System.out.println(node);
            }

        }

    }


    public static void main(String[] args) {
        JSONObject jsonData = parseJSONFile("src/RouterConfig.json");
        if(jsonData != null) {
            initRouters(jsonData);
            deriveSubnetsAndGenerateVectorEntries(jsonData);
        }else {
            System.out.println("Failed to parse file");
        }
    }
}
