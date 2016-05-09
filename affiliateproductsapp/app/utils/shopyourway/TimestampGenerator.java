package utils.shopyourway;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import play.Play;

public class TimestampGenerator {

    public static Timestamp generateTimestamp() {
        String timeZone = Play.configuration.getProperty("mag.shopyourway.timestamp.generator.time.zone");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        calendar.set(Calendar.MILLISECOND, 0);
        String datePattern = Play.configuration.getProperty("mag.shopyourway.timestamp.generator.date.pattern");
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        String formattedTimestamp = formatter.format(calendar.getTime());
        long timestamp;
        try {
            timestamp = formatter.parse(formattedTimestamp).getTime() / 1000;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Timestamp(timestamp, formattedTimestamp);
    }
}
