package utils;

public class UniqueIdGenerator {
    private static short lastNumber = 0;

    public static synchronized String getUniqueId() {
    	String ip = getIPAddress();
    	long time = System.currentTimeMillis();
    	short number = getNextNumber();

    	return String.format("%1$s.%2$d.%3$d", ip, new Long(time), new Short(number));
    }

    private static String getIPAddress() {
        String result = null;
        if (play.mvc.Http.Request.current() != null) {
        	result = play.mvc.Http.Request.current().remoteAddress;
        }
        return result;
    }

    private static synchronized short getNextNumber() {
    	lastNumber = (lastNumber >= Short.MAX_VALUE - 1) ? 0 : ++lastNumber;

    	return lastNumber;
    }
}
