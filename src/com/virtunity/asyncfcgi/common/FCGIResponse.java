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

import org.apache.mina.core.buffer.IoBuffer;

/**
 * A FastCGI response.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIResponse implements Serializable {
    public final static int FCGI_REQUEST_COMPLETE = 0;
    public final static int FCGI_CANT_MPX_CONN = 1;
    public final static int FCGI_OVERLOADED = 2;
    public final static int FCGI_UNKNOWN_ROLE = 3;    
    
    private int id = 0;
    private int application_status = 0;
    private FCGIProtocolStatus protocol_status = FCGIProtocolStatus.REQUEST_COMPLETE;
    private IoBuffer stdout = null;
    private IoBuffer stderr = null;
    private Object attachment = null;
    
    /**
     * Create new response instance.
     */
    public FCGIResponse() {
    }
    
    /**
     * Create new response with passed FCGIRequest ID
     * @param fcgiRequestId
     */
    public FCGIResponse(int fcgiRequestId) {
    	setId(fcgiRequestId);
    }
    
    /**
     * Get the request id.
     *
     * @return Request ID or 0 if not available.
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Set set request id (usually copied from a {@link FCGIRequest}).
     *
     * @param id Must be greated than 0 and not bigger than 0xFFFF.
     */
    public void setId(int id) {
        this.id = (id == 0xFFFF) ? 0xFFFF : id % 0xFFFF;
    }
    
    /**
     * Get application status.
     *
     * @see {@link FCGIResponse.setApplicationStatus}
     */
    public int getApplicationStatus() {
        return application_status;
    }
    
    /**
     * Set application status for this response.
     *
     * Usually this is the status code the FastCGI backend generated during execution.
     */
    public void setApplicationStatus(int status) {
        this.application_status = status;
    }
    
    /**
     * Get protocol status.
     *
     * @see {@link FCGIResponse.setProtocolStatus}
     */
    public FCGIProtocolStatus getProtocolStatus() {
        return this.protocol_status;
    }
    
    /**
     * Set protocol status for this response.
     */
    public void setProtocolStatus(FCGIProtocolStatus status) {
        this.protocol_status = status;
    }
    
    /**
     * Get standard output.
     *
     * This is the content returned by the fastcgi server.
     */
    public IoBuffer getStdout() {
        return stdout;
    }
    
    /**
     * Set standard output.
     *
     * @see {@link FCGIResponse.getStdout}
     */
    public void setStdout(IoBuffer stdout) {
        this.stdout = stdout;
    }
    
    /**
     * Get standard error.
     *
     * This is the standard error output the backend emitted when processing the request.
     */
    public IoBuffer getStderr() {
        return stderr;
    }
    
    /**
     * Set standard error.
     *
     * @see {@link FCGIResponse.getStderr}
     */
    public void setStderr(IoBuffer stderr) {
        this.stderr = stderr;
    }
    
    /**
     * Print debug information about this response object {@link System.out}.
     */
    public void printDebug() {
        printDebug(System.out);
    }
    
    /**
     * Print debug information about this response object to the specified {@link PrintStream}.
     */
    public void printDebug(PrintStream out) {
        out.println("FCGIResponse:");
        out.println("  Request-ID     = "+this.id);
        out.println("  AppStatus      = "+ this.application_status);
        out.println("  ProtocolStatus = "+this.protocol_status);
        out.println("  Stdout         = "+((this.stdout != null) ? this.stdout.capacity() : 0)+" bytes");
        out.println("  Stderr         = "+((this.stderr != null) ? this.stderr.capacity() : 0)+" bytes");
    }
    
    /**
     * Returns a textual representation of this object.
     */
    public String toString() {
        return "FCGIResponse("+this.id+")";
    }
}
