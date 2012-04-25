package net.igorclark.minagor.application;

public class EnvConstants {
	
	public static final String	REQUEST_METHOD					= "REQUEST_METHOD";
	public static final String	QUERY_STRING					= "QUERY_STRING";
	public static final String	CONTENT_TYPE					= "CONTENT_TYPE";
	public static final String	CONTENT_LENGTH					= "CONTENT_LENGTH";
	public static final String	POST							= "POST";
	public static final String	GET								= "GET";
	public static final String	CONTENT_TYPE_URL_ENCODED		= "application/x-www-form-urlencoded";
	
	public static final String	CONTENT_TYPE_MULTIPART_FORM_DATA				= "multipart/form-data";
	public static final int		CONTENT_TYPE_MULTIPART_FORM_DATA_LABEL_LENGTH	= CONTENT_TYPE_MULTIPART_FORM_DATA.length();
	
	public static final String	CONTENT_TYPE_MULTIPART_FORM_DATA_BOUNDARY				= "multipart/form-data; boundary=";
	public static final int		CONTENT_TYPE_MULTIPART_FORM_DATA_BOUNDARY_LABEL_LENGTH	= CONTENT_TYPE_MULTIPART_FORM_DATA_BOUNDARY.length();
	
}
