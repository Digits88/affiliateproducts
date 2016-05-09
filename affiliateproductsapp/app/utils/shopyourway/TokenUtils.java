package utils.shopyourway;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

public class TokenUtils {

	public static final String USER_ID_SEPARATOR = "_";
	
	private static final String ZERO = "0";

	@Nullable
	public static Long getUserId(String token) {
		String userId = StringUtils.substringBefore(token, USER_ID_SEPARATOR);
		try {
			return Long.valueOf(userId);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static boolean isAnonymousToken(String token) {
		boolean isAnonymous = false;
		String userId = StringUtils.substringBefore(token, USER_ID_SEPARATOR);
		if (StringUtils.isNotBlank(userId) && ZERO.equals(userId)) {
			isAnonymous = true;
		}
		return isAnonymous;
	}
}
