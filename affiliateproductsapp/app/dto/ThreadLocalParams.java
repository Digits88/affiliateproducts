package dto;

import models.shopyourway.SywUser;


public class ThreadLocalParams {

	private String token;
	private Long userId;
	private String requestId;
	private String source;
	private String channel;
	private SywUser user;

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	public SywUser getUser() {
		return user;
	}

	public void setUser(SywUser user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		int result = requestId != null ? requestId.hashCode() : 0;
		return result;
	}

	@Override
	public boolean equals(Object object) {
		boolean result = false;

		if ((object != null) && (object instanceof ThreadLocalParams)) {
			ThreadLocalParams thatToken = (ThreadLocalParams) object;
			result = ((isNullEquals(userId, thatToken.getUserId())) && (isNullEquals(
					requestId, thatToken.getRequestId())));
		}

		return result;
	}

	private boolean isNullEquals(Object obj1, Object obj2) {
		boolean result = (obj1 == null) && (obj2 == null);

		if (obj1 != null) {
			result = obj1.equals(obj2);
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(this.requestId).append(" - ").append(this.userId);
		return stringBuffer.toString();
	}

}
