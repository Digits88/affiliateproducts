package models.log;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import play.data.validation.IPv6Address;
import utils.TL;

@Entity
@Table(name = "AFFILIATE_LOG_REQUESTS")
public class LogRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String SOURCE_PARAM_NAME = "source";
	public static final String CHANNEL_PARAM_NAME = "channel";
	public static final String BODY_PARAM_NAME = "body";

	@Id
	@GeneratedValue
	private Long id;

	@Column(name="server_host")
	private String serverHost;
	
	@IPv6Address
	@Column(name="ip_address")
	private String ipAdress;

	@Column(name="source")
	private String source;

	@Column(name = "channel")
	private String channel;
	
	@Column(name = "user_id")
	private Long userId;
	
	@Column(name="request_id")
	private String requestId;

	@Column(name = "http_method")
	private String httpMethod;

	@Column(name = "path")
	private String path;
	
	@Column(name = "class")
	private String className;
	
	@Column(name = "method")
	private String methodName;

	@Temporal(TemporalType.TIMESTAMP)
	@JoinColumn(name = "begin")
	private Date begin;

	@Temporal(TemporalType.TIMESTAMP)
	@JoinColumn(name = "end")
	private Date end;

	@Column(name = "elapsed_time")
	private Long elapsedTime;
	
	@Column(name = "message")
	private String message;

	@Column(name = "params", length = 2048)
	private String params;

	@Column(name = "exception", length = 2048)
	private String exception;

	public LogRequest() {
		this.userId = TL.getUserId();
		this.requestId = TL.getRequestId();
		this.source = TL.getSource();
		this.channel = TL.getChannel();
	}
	
	public LogRequest(String className, String methodName, String message, String params) {
		this();
		this.className = className;
		this.methodName = methodName;
		this.message = message;
		this.params = params;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}
	
	public String getSource() {
		return source;
	}

	public String getChannel() {
		return channel;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(Long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		if (params != null && !params.isEmpty()) {
			this.params = StringUtils.substring(params, 0, 2048);
		}
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = StringUtils.substring(exception, 0, 2048);
	}

	public String getRequestId() {
		return requestId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getIpAdress() {
		return ipAdress;
	}

	public void setIpAdress(String ipAdress) {
		this.ipAdress = ipAdress;
	}
	
}
