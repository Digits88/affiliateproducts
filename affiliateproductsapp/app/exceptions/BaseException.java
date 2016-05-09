package exceptions;

public class BaseException extends RuntimeException {

	private Object[] params;

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Object... params) {
		super(message);
		this.params = params;
	}

	public Object[] getParams() {
		return params;
	}
}
