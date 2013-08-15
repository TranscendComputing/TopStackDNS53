package com.msi.dns53.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ErrorResponse", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
public class ErrorResponsePOJO {
	private ErrorPOJO error;
	private String requestId;
	
	@XmlRegistry
	public class ObjectFactory {
		public ErrorResponsePOJO createErrorResponsePOJO() { return new ErrorResponsePOJO(); }
	}
	
	@XmlElement(name = "Error", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public ErrorPOJO getError() {
		return error;
	}

	public void setError(ErrorPOJO error) {
		this.error = error;
	}

	@XmlElement(name = "RequestId", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


	public static class ErrorPOJO{
		private String type;
		private String code;
		private String message;
		
		@XmlElement(name = "Type", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		@XmlElement(name = "Code", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		@XmlElement(name = "Message", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		
	}
}
