package models.shopyourway;

import java.io.Serializable;

public class SywError implements Serializable {

	private static final long serialVersionUID = 1L;

	private ErrorMsg error;

	public ErrorMsg getError() {
		return error;
	}

	public void setError(ErrorMsg error) {
		this.error = error;
	}

	public class ErrorMsg implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long statusCode;
		private String message;
		private String requestId;

		public Long getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(Long statusCode) {
			this.statusCode = statusCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(String requestId) {
			this.requestId = requestId;
		}
	}
}
