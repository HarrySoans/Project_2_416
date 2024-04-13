import java.util.Map;

public class DistanceVector {
    String senderName;
    Map<String, VectorEntry> distanceVector;

    DistanceVector(String senderName, Map<String, VectorEntry> distanceVector) {
        this.senderName = senderName;
        this.distanceVector = distanceVector;
    }

    public void addEntry(String subnet, VectorEntry entry) {
        distanceVector.put(subnet, entry);
    }

    public Map<String, VectorEntry> getDV() {
        return this.distanceVector;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sender Name: ").append(senderName).append("\n");
        sb.append("Distance Vector:\n");
        for (Map.Entry<String, VectorEntry> entry : distanceVector.entrySet()) {
            VectorEntry ve = entry.getValue();
            String stringVE = ve.toStringEntry();
            sb.append("  Subnet: ").append(entry.getKey()).append(", VectorEntry: ").append(stringVE).append("\n");
        }
        return sb.toString();
    }



}
