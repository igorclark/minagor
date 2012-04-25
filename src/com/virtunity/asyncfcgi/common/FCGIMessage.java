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
 * A low level FastCGI message.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIMessage implements Serializable  {
    /** Protocol version (always 1) */
    public final static int VERSION = 1;
    /** Header length */
    public final static int HEADERLEN = 8;
    /** Maximum overall message length */
    public final static int MAXLEN = 0xFFFF;
    
    private int version = 1;
    private FCGIMessageType type;
    private int request_id = 0;
    private int content_length = 0;
    private int padding_length = 0;
    private byte reserved = 0;
    private IoBuffer content = null;
    
    /**
     * Create a new message.
     */
    public FCGIMessage(FCGIMessageType type) {
        this.type = type;
    }
    
    /**
     * Get FastCGI protocol version (usually 1).
     */
    public int getVersion() {
        return this.version;
    }
    
    /**
     * Set FastCGI protocol version.
     *
     * There is no good reason for setting this to something else than 1.
     */
    public void setVersion(int version) {
        this.version = version;
    }
    
    /**
     * Get message type.
     */
    public FCGIMessageType getType() {
        return this.type;
    }
    
    /**
     * Set message type
     */
    public void setType(FCGIMessageType type) {
        this.type = type;
    }
    
    /**
     * Get request id.
     *
     * @return Request id bigger than 0 but not bigger than 0xFFFF.
     */
    public int getRequestId() {
        return this.request_id;
    }
    
    /**
     * Set request id.
     *
     * @param request_id Request id bigger than 0 but not bigger than 0xFFFF.
     */
    public void setRequestId(int request_id) {
        if (request_id > 0xFFFF) {
            throw (new NumberFormatException("Request ID must be greated than 0 and must not be bigger than "+0xFFFF+" (0xFFFF)"));
        }
        this.request_id = request_id;
    }
    
    /**
     * Get content length.
     */
    public int getContentLength() {
        return this.content_length;
    }
    
    /**
     * Set content length.
     *
     * This is used internally to populate the message correctly.
     */
    public void setContentLength(int length) {
        this.content_length = length;
    }
    
    /**
     * Get message content.
     */
    public IoBuffer getContent() {
        return this.content;
    }
    
    /**
     * Set messge content.
     */     
    public void setContent(IoBuffer content) {
        this.content = content;
        if (content == null) {
            this.content_length = 0;
        } else {
            this.content_length = content.limit();
        }
    }
    
    /**
     * Get padding length.
     */
    public int getPaddingLength() {
        return this.padding_length;
    }
    
    /**
     * Set padding length.
     *
     * This is used internally to populate the message correctly.
     */
    public void setPaddingLength(int length) {
        this.padding_length = length;
    }
    
    /**
     * Get reserved field value.
     *
     * There is no special purpose for the reserved field. All FastCGI
     * implementations I am aware of simply set this to 0.
     */
    public byte getReserved() {
        return this.reserved;
    }
    
    /**
     * Set reserved field value.
     *
     * @see {@link FCGIMessage.getReserved)
     */
    public void setReserved(byte reserved) {
        this.reserved = reserved;
    }
    
    /**
     * Print debugging output of the specified byte array to System.out.
     */
    public static void printDebug(byte[] buf) {
        if (buf == null) {
            System.out.println("<Empty buffer>");
            return;
        }
        for (int i=0; i<buf.length; i++) {
            
            String s = Integer.toHexString((buf[i] < 0) ? buf[i]+256 : buf[i]);
            if (s.length() == 1) s = "0"+s;
            System.out.print(s+" ");
        }
        System.out.println("");
        for (int i=0; i<buf.length; i++) {
            char c = (char) buf[i];
            if (c < 21) c = '.';
            System.out.print(" "+c+" ");
        }
        System.out.println("");
    }
    
    /**
     * Print debugging output of the specified IoBuffer to System.out.
     */
    public static void printDebug(IoBuffer buf) {
        if (buf == null) {
            System.out.println("<Empty buffer>");
            return;
        }
        System.out.println("Buffer position="+buf.position()+" limit="+buf.limit()+" capacity="+buf.capacity());
        int pos = buf.position();

        buf.position(0);
        byte[] content = new byte[buf.limit()];
        buf.get(content);
        buf.position(pos);
        printDebug(content);
    }
    
    /**
     * Print debug information about this message object to {@link System.out}.
     */
    public void printDebug() {
        printDebug(System.out);
    }
    
    /**
     * Print debug information about this message object to the specified {@link PrintStream}.
     */
    public void printDebug(PrintStream out) {
        out.println("FCGIMessage:");
        out.println("  version     = "+this.version);
        out.println("  type        = "+this.type);
        out.println("  request_id  = "+this.request_id);
        out.println("  content_len = "+this.content_length);
        out.println("  padding_len = "+this.padding_length);
    }
}
