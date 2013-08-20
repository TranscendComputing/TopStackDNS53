/*
 * TopStack (c) Copyright 2012-2013 Transcend Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
