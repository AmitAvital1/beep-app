package beep.app.login;

public class RegionItem {
    private String prefix;
    private int regionNumber;
    private int flagResourceId;

    public RegionItem(String prefix, int regionNumber, int flagResourceId) {
        this.prefix = prefix;
        this.regionNumber = regionNumber;
        this.flagResourceId = flagResourceId;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getRegionNumber() {
        return regionNumber;
    }

    public int getFlagResourceId() {
        return flagResourceId;
    }
}
