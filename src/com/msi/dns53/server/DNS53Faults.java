package com.msi.dns53.server;

import com.msi.tough.query.ErrorResponse;

public class DNS53Faults {
	/*public static ErrorResponse AccessDenied() {
		return new ErrorResponse(
				"Sender",
				"Access denied.",
				"AccessDenied",
				403).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InappropriateXML() {
		return new ErrorResponse(
				"Sender",
				"The XML document you provided was well-formed and valid, but not appropriate for this operation.",
				"InappropriateXML").withXmlns(DNS53Constants.XMLNS_VALUE);
	}*/
	public static ErrorResponse InternalError() {
		return new ErrorResponse(
				"Sender",
				"We encountered an internal error. Please try again.",
				"InternalError", 500).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidAction() {
		return new ErrorResponse(
				"Sender",
				"The action specified is not valid.",
				"InvalidAction").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidArgument(String msg) {
		return new ErrorResponse(
				"Sender",
				msg,
				"InvalidArgument").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse UnrecognizedClient() {
		return new ErrorResponse(
				"Sender",
				"The security token included in the request is invalid.",
				"UnrecognizedClient", 403).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse MissingAuthenticationToken() {
		return new ErrorResponse(
				"Sender",
				"The HTTP authorization header is bad, use the format: " +
				"AWS3-HTTPS AWSAccessKeyId=AccessKey, Algorithm=ALGORITHM, " +
				"Signature=Base64( Algorithm((ValueOfDateHeader), SigningKey) )",
				"MissingAuthenticationToken", 403).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidHTTPRequest() {
		return new ErrorResponse(
				"Sender",
				"There was an error in the body of your HTTP request.",
				"InvalidHTTPRequest").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidURI() {
		return new ErrorResponse(
				"Sender",
				"Could not parse the specified URI.",
				"InvalidURI").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	/*public static ErrorResponse MalformedXML() {
		return new ErrorResponse(
				"Sender",
				"The XML you provided was not well-formed or did not validate against our published schema.",
				"MalformedXML").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse MalformedXML(String msg) {
		return new ErrorResponse(
				"Sender",
				msg,
				"MalformedXML").withXmlns(DNS53Constants.XMLNS_VALUE);
	}*/
	public static ErrorResponse MissingRequiredParameter(String msg) {
		return new ErrorResponse(
				"Sender",
				msg,
				"MissingRequiredParameter").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse NotImplemented() {
		return new ErrorResponse(
				"Sender",
				"Not implemented.",
				"NotImplemented", 501).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse OptInRequired() {
		return new ErrorResponse(
				"Sender",
				"The AWS Access Key ID needs a subscription for the service.",
				"OptInRequired", 403).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse RequestExpired(String timestamp) {
		return new ErrorResponse(
				"Sender",
				"Request has expired. Time stamp date is " + timestamp + ".",
				"RequestExpired").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidSignature() {
		return new ErrorResponse(
				"Sender",
				"The request signature Amazon Route 53 calculated does not match the signature you provided. " +
				"Check your AWS Secret Access Key and signing method. Consult the service documentation for details.",
				"InvalidSignature", 403).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidChangeBatch(String msg) {
		return new ErrorResponse(
				"Sender",
				msg,
				"InvalidChangeBatch").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidDomainName() {
		return new ErrorResponse(
				"Sender",
				"The specified domain name is not valid.",
				"InvalidDomainName").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	/*public static ErrorResponse DelegationSetNotFound() {
		return new ErrorResponse(
				"Sender",
				"Amazon Route 53 allows some duplication, but Amazon Route 53 has a maximum threshold of duplicated domains. " +
				"This error is generated when you reach that threshold. In this case, the error indicates that too many hosted " +
				"zones with the given domain name exist. If you want to create a hosted zone and Amazon Route 53 generates " +
				"this error, contact Customer Support.",
				"DelegationSetNotFound").withXmlns(DNS53Constants.XMLNS_VALUE);
	}*/
	public static ErrorResponse HostedZoneAlreadyExists() {
		return new ErrorResponse(
				"Sender",
				"The hosted zone you are attempting to create already exists. " +
				"Amazon Route 53 returns this error when a hosted zone has already been created with the supplied CallerReference.",
				"HostedZoneAlreadyExists").withXmlns(DNS53Constants.XMLNS_VALUE).withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidInput() {
		return new ErrorResponse(
				"Sender",
				null,
				"InvalidInput").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse InvalidInput(String msg) {
		return new ErrorResponse(
				"Sender",
				msg,
				"InvalidInput").withXmlns(DNS53Constants.XMLNS_VALUE);
	}	
	public static ErrorResponse DelegationSetNotAvailable() {
		return new ErrorResponse(
				"Sender",
				"The hosted zone you are attempting to create already exists. No more duplicate hosted zones with the same name can be created.",
				"DelegationSetNotAvailable").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse NoSuchHostedZone() {
		return new ErrorResponse(
				"Sender",
				"No hosted zone found with such ID.",
				"NoSuchHostedZone").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse NoSuchHostedZone(String hostedZoneId) {
		return new ErrorResponse(
				"Sender",
				"No hosted zone found with such ID:" + hostedZoneId + ".",
				"NoSuchHostedZone").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse NoSuchChange() {
		return new ErrorResponse(
				"Sender",
				"No change found with such ID.",
				"NoSuchHostedZone").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
	public static ErrorResponse NoSuchChange(String id) {
		return new ErrorResponse(
				"Sender",
				"No change found with such ID:" + id + ".",
				"NoSuchHostedZone").withXmlns(DNS53Constants.XMLNS_VALUE);
	}
}
