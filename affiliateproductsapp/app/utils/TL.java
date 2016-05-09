package utils;

import models.shopyourway.SywUser;
import dto.ThreadLocalParams;

//import play.Logger;

public class TL {

	private static ThreadLocal<ThreadLocalParams> threadSession = new ThreadLocal();

	public static void set(Long userId, String requestId) {
		get().setUserId(userId);
		get().setRequestId(requestId);
	}
	
	public static void setToken(String token) {
		get().setToken(token);
	}
	public static void setRequestId(String requestId) {
		get().setRequestId(requestId);
	}
	
	public static void setUserId(Long userId) {
		get().setUserId(userId);
	}
	
	public static void setSource(String source) {
		get().setSource(source);
	}
	
	public static void setChannel(String channel) {
		get().setChannel(channel);
	}
	
	public static void setUser(SywUser user) {
		get().setUser(user);
	}
	
	public static ThreadLocalParams get() {
		ThreadLocalParams localParams = threadSession.get();
		if (localParams == null) {
			localParams = new ThreadLocalParams();
        	threadSession.set(localParams);
		}
		return localParams;
	}
	
	public static String getToken() {
		return get().getToken();
	}
	public static Long getUserId() {
		return get().getUserId();
	}
	
	public static String getRequestId() {
		return get().getRequestId();
	}
	
	public static String getSource() {
		return get().getSource();
	}
	
	public static String getChannel() {
		return get().getChannel();
	}
	
	public static SywUser getUser() {
		return get().getUser();
	}
	
	public static void remove() {
		threadSession.remove();
	}
}

