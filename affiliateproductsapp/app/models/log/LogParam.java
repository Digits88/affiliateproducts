package models.log;

import com.google.common.base.Objects;

public class LogParam {
	
	private String key;
	private Object value;
	
	public LogParam(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public static LogParam get(String key, Object value) {
		return new LogParam(key, value);
	}
	public String toString() {
		return Objects.toStringHelper(this)
				.add(key, value)
				.toString();
	}
	

}
