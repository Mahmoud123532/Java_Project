package soccer.play;

public class DisplayString implements IDisplayDataItem {
    private int id;
    private String text;

    public DisplayString(int id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override public boolean isDetailAvailable() { return false; }
    @Override public String getDisplayDetail() { return text; }
    @Override public int getID() { return id; }
    @Override public String getDetailType() { return "text"; }
    @Override public String toString() { return text; }
}
