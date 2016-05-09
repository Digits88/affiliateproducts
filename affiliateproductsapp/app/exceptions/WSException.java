package exceptions;

import models.shopyourway.WSError;

public class WSException extends ServiceException {

	private WSError error;

	public WSException(String message) {
		super(message);
	}

	public WSException(String message, Object... params) {
		super(message, params);
	}

	public WSException(WSError sywError) {
		super(sywError.getError().getMessage());
		this.error = sywError;
	}
}
