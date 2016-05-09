package utils.shopyourway;

public class Timestamp {

    private final long timestamp;
    private final String formattedTimestamp;

    public Timestamp(long timestamp, String formattedTimestamp) {
        this.timestamp = timestamp;
        this.formattedTimestamp = formattedTimestamp;
    }

    public long getAsLong() {
        return timestamp;
    }

    public String getAsFormattedString() {
        return formattedTimestamp;
    }
}
