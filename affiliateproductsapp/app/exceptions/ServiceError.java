package exceptions;


public class ServiceError extends BaseException {

	public ServiceError(String message) {
		super(message);
	}

	public ServiceError(String message, Object... params) {
		super(message, params);
	}
}
