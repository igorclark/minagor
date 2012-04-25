package net.igorclark.minagor.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.igorclark.minagor.mime.MultipartReader;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.util.URLUtil;

import com.virtunity.asyncfcgi.common.FCGIRequest;
import com.virtunity.asyncfcgi.common.FCGIResponse;

/**
 * A Cycle represents a FastCGI request-response cycle.
 * @author igor
 */
public class Cycle {
	
	/**
	 * FIXME
	 * Need to make this work so that the internal Map can be <String, Object>
	 * - or work out some other way to do something useful with the uploaded data
	 * and just return string info about it.
	 */
	private Map<String, Map<String, String>>	_requestParameters = new HashMap<String, Map<String, String>>();
	public	Map<String, Map<String, String>>	requestParameters() {
		return _requestParameters;
	}
	private	String		_requestMethod;
	
    private	Boolean	_multipartDataUploaded;
    public	boolean	multipartDataUploaded() {
    	return _multipartDataUploaded == null ? false : _multipartDataUploaded.booleanValue();
    }
    	
	private FCGIResponse	_fcgiResponse;
	private FCGIRequest		_fcgiRequest;
	
	/**
	 * Constructor sets up I/O and reads in parameters from request.
	 * @param req The incoming FCGIRequest
	 * @param rsp The outgoing FCGIResponse
	 */
	public Cycle(FCGIRequest req, FCGIResponse rsp) {
		this._fcgiRequest	=	req;
		this._fcgiResponse	=	rsp;
		this.readRequestParameters();
	}
	
	/**
	 * Read in HTTP GET/POST params & data, and uploaded multipart/form-data if present
	 */
	private void readRequestParameters() {
    	// Get HTTP environment variables
    	Map<String, String> params		= this._fcgiRequest.getParams();
    	
        // read POSTed content/GET parameters?
        String requestMethod	= params.get(EnvConstants.REQUEST_METHOD);
        
        // GET
        if(requestMethod.equals(EnvConstants.GET)) {
        	// read query string
        	String parameterData = params.get(EnvConstants.QUERY_STRING);
        	if(parameterData.length() > 0) {
        		_requestMethod		= EnvConstants.GET;
        		_requestParameters.put(EnvConstants.GET, processHttpParameterString(parameterData));
        	}
        	return;
        }

        // POST
        if (requestMethod.equals(EnvConstants.POST)) {
        	_requestMethod = EnvConstants.POST;
        	parsePostedData();
        }

	}
	
	/**
	 * Work out whether a simple url-encoded form submission or a file upload, parse, and deal with.
	 */
	public void parsePostedData() {
    	String	contentType		= this._fcgiRequest.getParams().get(EnvConstants.CONTENT_TYPE);
			int 	contentLength	= Integer.parseInt(this._fcgiRequest.getParams().get(EnvConstants.CONTENT_LENGTH));
    	
    	// posted data, multipart / file upload
    	if(
    			contentType.length() >= EnvConstants.CONTENT_TYPE_MULTIPART_FORM_DATA_LABEL_LENGTH
    			&& contentType.substring(0, EnvConstants.CONTENT_TYPE_MULTIPART_FORM_DATA_LABEL_LENGTH).equals(
    					EnvConstants.CONTENT_TYPE_MULTIPART_FORM_DATA
    				)
    	) {
    		// read multipart data
    		_multipartDataUploaded	= Boolean.TRUE;
    		try {
    			_requestParameters.put(EnvConstants.POST, processMultipartFormData(contentLength));
    		}
    		catch(IOException ioe) {
    			System.out.println("IOException: " + ioe.getLocalizedMessage());
    		}
    	}
    	
    	// posted data, name/value pairs
    	else if(contentType.equals(EnvConstants.CONTENT_TYPE_URL_ENCODED)) {
   			byte[] data = new byte[contentLength];
   			try {
   				if(this._fcgiRequest.getStdin().asInputStream().read(data, 0, contentLength)>0) {
   					try {
   						_requestParameters.put(EnvConstants.POST, processHttpParameterString(new String(data)));
   					}
   					catch(IllegalArgumentException i) {
   						System.out.println(i.getMessage());
   					}
   				}
   			}
   			catch(IOException ioe) {
   				System.out.println("IOException: " + ioe.getLocalizedMessage());
   			}
    	}
	}
	
	/**
	 * Parse a URL-encoded name/value pair string into a hash map
	 * @param parameters
	 * @return
	 * @throws IllegalArgumentException
	 */
	private HashMap<String, String> processHttpParameterString(String parameters) throws IllegalArgumentException {
		if(parameters.length()==0) {
			return null;
		}
		HashMap<String, String> h = new HashMap<String, String>();
		String[] pairs = parameters.split("&");
		for(int i=0;i<pairs.length;i++) {
			String[] pair = pairs[i].split("=");
			if(pair.length != 2) {
				throw new IllegalArgumentException("Malformed parameter string."); //FIXME
			}
			h.put(pair[0], URLUtil.URLDecode(pair[1]));
		}
		return h;
	}

    /**
	 * Process POSTed multipart/form-data.
	 * @param contentLen
	 * @return A Hashtable containing the processed parts, or filenames of uploaded files
	 * @throws IOException
	 */
	private HashMap<String, String> processMultipartFormData(int httpContentLength) throws IOException {
    	Map<String, String>	params			= this._fcgiRequest.getParams();
		String				contentType		= (String)params.get(EnvConstants.CONTENT_TYPE);
		int					contentLength	= Integer.parseInt((String)params.get(EnvConstants.CONTENT_LENGTH));
		String				boundary		= "--" + contentType.substring(EnvConstants.CONTENT_TYPE_MULTIPART_FORM_DATA_BOUNDARY_LABEL_LENGTH);
		return new MultipartReader().parseMultipartData(this._fcgiRequest.getStdin().asInputStream(), contentType, boundary);
	}
	
    /**
     * This is the bit that needs to *do* something
     */
	public	void	process() {        
        DispatchManager	dispatchManager	=	DispatchManager.defaultDispatchManager();
        Dispatcher		dispatcher		=	dispatchManager.getDispatcher(_fcgiRequest);
        
        dispatcher.setSession(Application.defaultApplication().sessionStore().getSession(_fcgiRequest));
        dispatcher.setRequestParameters(_requestParameters);
        
        _fcgiResponse.setStdout(IoBuffer.wrap(dispatcher.dispatch()));
        _fcgiResponse.setStderr(IoBuffer.wrap(dispatcher.getStdErr()));
	}

}
