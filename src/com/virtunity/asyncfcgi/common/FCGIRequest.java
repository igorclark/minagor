/*
 * Copyright 2008 Daniel Wirtz (daniel@virtunity.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.virtunity.asyncfcgi.common;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * A FastCGI request.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIRequest implements Serializable {
    /** Request ID */
    private int id = 1;
    /** Standard input contents */
    private IoBuffer stdin = null;
    /** Request parameters */
    private Map<String,String> params = null;
    /** Request role */
    private FCGIRole role = FCGIRole.RESPONDER;
    /** Request flags */
    private int flags = FCGIRequestFlag.KEEP_CON.getValue();
    /** Attachment */
    private Object attachment = null;
    
    /**
     * Create new request instance.
     */
    public FCGIRequest() {
    }
    
    /**
     * Get the request id.
     *
     * @return Request ID or 1 by default if none has been set manually.
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Set request id.
     *
     * @param id Must be greated than 0 and not bigger than 0xFFFF.
     */
    public void setId(int id) {
        this.id = (id == 0xFFFF) ? 0xFFFF : id % 0xFFFF;
    }
    
    /**
     * Get request role.
     *
     * @return This request's role or RESPONDER if none has been set.
     */
    public FCGIRole getRole() {
        return this.role;
    }
    
    /**
     * Set request role.
     *
     * @param role One of FCGI_RESPONDER, FCGI_AUTHORIZER or FCGI_FILTER. If
     *             none has been set, FCGI_RESPONDER will be the default.
     */
    public void setRole(FCGIRole role) {
        this.role = role;
    }
    
    /**
     * Get request flags.
     */
    public int getFlags() {
        return this.flags;
    }
    
    /**
     * Set request flags.
     *
     * This is a low level protocol setting. Don't mess with it unless you know
     * exactly what you are doing.
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }
    
    /**
     * Get all request parameters.
     * 
     * @see {@link FCGIRequest.setParams}
     * @return Map containing the parameters
     */
    public Map<String,String> getParams() {
        return this.params;
    }
    
    /**
     * Get specific request parameter.
     *
     * @see {@link FCGIRequest.setParams}
     * @return Parameter value or null if it does not exist
     */
    public String getParam(String key) {
        if (this.params == null) return null;
        return this.params.get(key);
    }
    
    /**
     * Set all request parameters at once.
     *
     * @see {@link FCGIRequest.setParams}
     * @param params Map containing the parameters
     */
    public void setParams(Map<String,String> params) {
        this.params = params;
    }
    
    /**
     * Set specific request parameter.
     *
     * Valid known basic parameters e.g. those understood by PHP or Ruby backends:<br />
     * <pre>
     * SCRIPT_FILENAME    The complete physical filename
     * DOCUMENT_ROOT      The physical document root
     * REQUEST_URI        The request URI including query parameters
     * QUERY_STRING       Query parameters only (everything behind the ? character in REQUEST_URI)
     * SERVER_NAME        Name of the fastcgi client talking to the backend, usually its IP address or hostname
     * GATEWAY_INTERFACE  Usually "CGI/1.1"
     * REQUEST_METHOD     HTTP request method (e.g. GET, POST etc.)
     * SERVER_SOFTWARE    Server software name including version
     * SERVER_ADDR        The fastcgi client's ip address
     * SERVER_PORT        The fastcgi client's port
     * REMOTE_ADDR        E.g. the HTTP client's remote ip address
     * REMOTE_PORT        E.g. the HTTP client's remote port
     * </pre>
     * Syntax of HTTP parameters is basically: HTTP_ + value.replace("-", "_").toUpper(), e.g.
     * <pre>
     * HTTP_HOST          HTTP host parameter
     * HTTP_ACCEPT        HTTP accept parameter
     * ...
     * </pre>
     * 
     * @param key Parameter key
     * @param value Parameter value
     */
    public void setParam(String key, String value) {
        if (this.params == null) {
            this.params = new HashMap<String,String>();
        }
        this.params.put(key, value);
    }
    
    /**
     * Get standard input.
     *
     * Standard input is sent within {@link FCGIRequest} messages e.g. for
     * POST content or uploaded files.
     */
    public IoBuffer getStdin() {
        return this.stdin;
    }
    
    /**
     * Reuse another buffer as standard input.
     *
     * @see {@link FCGIRequest.getStdin}
     */
    public void setStdin(IoBuffer stdin) {
        this.stdin = stdin;
    }
    
    /**
     * Attach an object to this request.
     * 
     * @param o The attached object
     */
    public void setAttachment(Object o) {
        this.attachment = o;
    }
    
    /**
     * Get the attached object.
     * 
     * @return The attached object or null if none
     */
    public Object getAttachment() {
        return this.attachment;
    }
        
    /**
     * Print debug information about this request object to {@link System.out}.
     *
     * @see {@link FCGIRequest.printDebug(PrintStream)}
     */
    public void printDebug() {
        printDebug(System.out);
    }
    
    /**
     * Print debug information about this request object to the specified {@link PrintStream}.
     */
    public void printDebug(PrintStream out) {
        out.println("FCGIRequest:");
        out.println("  Request-ID     = "+this.id);
        out.println("  Role           = "+this.role);
        out.println("  Params         = "+(this.params != null ? this.params.size() : 0)+" items");
        out.println("  Stdin          = "+((this.stdin != null) ? this.stdin.capacity() : 0)+" bytes");
    }
    
    /**
     * Returns a textual representation of this object.
     */
    public String toString() {
        return this.getClass().getName()+"(RequestID="+this.getId()+")";
    }
}
