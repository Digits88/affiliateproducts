package exceptions;

public class ServiceException extends BaseException {

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, Object... params) {
		super(message, params);
	}
}
