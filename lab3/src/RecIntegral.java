import java.util.ArrayList;

public class RecIntegral {
    private ArrayList<Main.Table> lastRecords;

    public RecIntegral(Main.Table o) {
        lastRecords = new ArrayList<>();
        lastRecords.add(o);
    }

    public Main.Table getLastRecords() {
        return lastRecords.get(0);
    }
}
