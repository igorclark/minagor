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

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;


/**
 * FastCGI ProtocolDecoder.
 *
 * Decodes {@link IoBuffer} content into {@link FCGIPacket}, {@link FCGIRequest}
 * or {@link FCGIResponse} objects depending on the constructor's parameters.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIDecoder implements ProtocolDecoder {
    private final static int STATE_READ_HEADER = 1;
    private final static int STATE_READ_CONTENT = 2;
    private final static int STATE_READ_PADDING = 3;
    
    /** Output class type*/
    private Class decoded_class = null;    
    /** Current statemachine state */
    private int state = STATE_READ_HEADER;    
    /** Currently processed message */
    private FCGIMessage message = null;    
    /** Decode buffer */
    private IoBuffer dbuffer;
    /** Assembler standard input */
    private IoBuffer stdin;
    /** Assembler standard output */
    private IoBuffer stdout;
    /** Assembler standard error */
    private IoBuffer stderr;
    /** Assembler parameters buffer */
    private IoBuffer paramsbuf;
    
    /**
     * Create a new decoder.
     *
     * @param decoded_class Either FCGIRequest.class, FCGIResponse.class or FCGIMessage.class.
     */
    public FCGIDecoder(Class decoded_class) {
        this.decoded_class = decoded_class;
        this.dbuffer = IoBuffer.allocate(8);
        this.dbuffer.order(ByteOrder.BIG_ENDIAN);
        this.dbuffer.clear();
    }
    
    public void dispose(IoSession session) {
        this.state = STATE_READ_HEADER;
        this.message = null;
        if (this.dbuffer != null) {
            this.dbuffer.free();
        }
        this.dbuffer = null;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput output) {
    }
    
    /**
     * Decode IoBuffer content into {@link FCGIMessage} objects.
     *
     * Additionally the deencoder is able to assemble {@link FCGIRequest} or {@link FCGIResponse}
     * objects depending on the decoder constructor parameter.
     */
    public void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) {
        
        // Enter the statemachine
        while (buffer.remaining() > 0) {
            switch (this.state) {
            
            // Read message header
            case STATE_READ_HEADER:
                int hremaining = FCGIMessage.HEADERLEN - this.dbuffer.position();       
                if (buffer.remaining() < hremaining) {
                    // Not enough bytes to complete the header, just copy and return
                    this.dbuffer.put(buffer);
                    return;
                }
                // Enough bytes received, set custom limits to get header bytes only
                int hreallimit = buffer.limit();
                buffer.limit(buffer.position() + hremaining);
                this.dbuffer.put(buffer);
                buffer.limit(hreallimit);
                this.dbuffer.flip();
                
                // Process the header (see FastCGI protocol specification for more details)
                this.message = new FCGIMessage(FCGIMessageType.UNKNOWN_TYPE);
                this.message.setVersion(this.dbuffer.get());
                this.message.setType(FCGIMessageType.forValue(this.dbuffer.get()));
                int request_id = this.dbuffer.getShort();
                this.message.setRequestId(request_id < 0 ? request_id + 0x10000 : request_id);
                int content_length = this.dbuffer.getShort();
                this.message.setContentLength(content_length < 0 ? content_length + 0x10000 : content_length);
                this.message.setPaddingLength(this.dbuffer.get()); // length < 8
                this.message.setReserved(this.dbuffer.get());
                this.state = STATE_READ_CONTENT;
                
                // Add content buffer to message for further read operations
                IoBuffer content = IoBuffer.allocate(this.message.getContentLength());
                content.clear();
                this.message.setContent(content);
                
                // Reset decode buffer
                this.dbuffer.clear();
            
            // Read message content
            case STATE_READ_CONTENT:
                int remaining = this.message.getContentLength() - this.message.getContent().position();
                int reallimit = buffer.limit();
                if (buffer.remaining() > remaining) {
                    // More content received than fitting into the current message
                    // so we set a custom limit and reset it afterwards.
                    buffer.limit(buffer.position() + remaining);
                }
                remaining -= buffer.remaining();
                this.message.getContent().put(buffer);
                buffer.limit(reallimit);
                if (remaining == 0) {
                    // Content completely received and maybe even more bytes are available
                    this.state = STATE_READ_PADDING;
                } else {
                    // There is still some content left that was not received yet
                    return;
                }
            
            // Read message padding
            case STATE_READ_PADDING:
                int premaining = this.message.getPaddingLength() - this.dbuffer.position();
                if (buffer.remaining() < premaining) {
                    // Not enough bytes to complete padding, just copy and return
                    this.dbuffer.put(buffer);
                    return;
                }
                // Enough bytes received, skip needless padding and reset decode buffer
                for (int i=0; i<premaining; i++) buffer.get();
                this.dbuffer.clear();
                
                // Create output
                if (this.decoded_class == FCGIMessage.class) {
                    // Using low level messages
                    this.message.getContent().flip();
                    output.write(this.message);
                } else {
                    // Using assembler to build a FCGIRequest or FCGIResponse
                    Object o = assemble(this.message);
                    if (o != null) output.write(o);
                }
                this.message = null;
                this.state = STATE_READ_HEADER;
            }
        }
    }
    
    /**
     * Assemble multiple {@link FCGIMessage} objects into one {@link FCGIResponse} or {@link FCGIRequest}.
     */
    private Object assemble(FCGIMessage message) {
        
        // Assemble FCGIResponse
        if (this.decoded_class == FCGIResponse.class) {
            
            // Read stdout
            if (FCGIMessageType.STDOUT.equals(message.getType())) {
                if (this.stdout == null) {
                    this.stdout = message.getContent(); // Reuse message buffer
                } else {
                    this.stdout.setAutoExpand(true);
                    this.stdout.put(message.getContent().flip());
                }
            
            // Read stderr
            } else if (FCGIMessageType.STDERR.equals(message.getType())) {
                if (this.stderr == null) {
                      this.stderr = message.getContent(); // Reuse message buffer
                } else {
                    this.stderr.setAutoExpand(true);
                    this.stderr.put(message.getContent().flip());
                }
            
            // Finish response
            } else if (FCGIMessageType.END_REQUEST.equals(message.getType())) {
                FCGIResponse response = new FCGIResponse();
                response.setId(message.getRequestId());
                if (this.stdout != null) {
                    response.setStdout(this.stdout.flip()); // Reuse assembler buffer
                    this.stdout = null;
                }
                if (this.stderr != null) {
                    response.setStderr(this.stderr.flip()); // Reuse assembler buffer
                    this.stderr = null;
                }
                IoBuffer content = message.getContent();
                if (content.position() == 8) {
                    content.order(ByteOrder.BIG_ENDIAN);
                    content.flip();
                    response.setApplicationStatus(content.getInt());
                    response.setProtocolStatus(FCGIProtocolStatus.forValue(content.get()));
                    // Ignore needless reserved bytes
                }
                return response;
            }
        
        // Assemble FCGIRequest
        } else if (this.decoded_class == FCGIRequest.class) {
            
            // Read params
            if (FCGIMessageType.PARAMS.equals(message.getType())) {
                if (this.paramsbuf == null) {
                    this.paramsbuf = message.getContent(); // Reuse message buffer
                } else {
                    this.paramsbuf.setAutoExpand(true);
                    this.paramsbuf.put(message.getContent().flip());
                }
            
            // Read stdin
            } else if (FCGIMessageType.STDIN.equals(message.getType())) {
                
                // Read normally
                if (message.getContentLength() > 0) {
                    if (this.stdin == null) {
                        this.stdin = message.getContent(); // Reuse message buffer
                    } else {
                        this.stdin.setAutoExpand(true);
                        this.stdin.put(message.getContent().flip());
                    }
                
                // Process end of stdin (end of request)
                } else {
                    FCGIRequest request = new FCGIRequest();
                    request.setId(message.getRequestId());
                    if (this.paramsbuf != null) {
                        request.setParams(readParams(this.paramsbuf.flip()));
                        this.paramsbuf = null;
                    }
                    if (this.stdin != null) {
                        request.setStdin(this.stdin.flip()); // Reuse assembler buffer
                        this.stdin = null;
                    }
                    return request;
                }
             }
        }
        return null;
    }
    
    /**
     * Read raw params into a Map.
     */
    private static Map<String,String> readParams(IoBuffer buffer) {
        Map<String,String> params = new HashMap<String,String>();
        if (buffer != null) {
            while (buffer.remaining() > 0) {
                // Length values are conditional, this means that if the highest
                // bit of the first byte is set, an integer value is present, else
                // the first byte already contains the length (max. 127)
                int name_length = buffer.get();
                if ((name_length & 0x80) != 0) {
                    buffer.position(buffer.position()-1);
                    name_length = buffer.getInt() & 0x7FFFFFFF; // First bit does not count
                }
                int value_length = buffer.get();
                if ((value_length & 0x80) != 0) {
                    buffer.position(buffer.position()-1);
                    value_length = buffer.getInt() & 0x7FFFFFFF;
                }
                // IoBuffer.getString() reads NUL-terminated strings, so to make sure
                // that all bytes will be read I construct this on my own.
                byte[] nb = new byte[name_length];
                buffer.get(nb);
                byte[] vb = new byte[value_length];
                buffer.get(vb);
                try {
                    String key = new String(nb, "UTF-8");
                    String value = new String(vb, "UTF-8");
                    params.put(key, value);
                } catch (UnsupportedEncodingException ex) {
                    // UTF-8 -> Never thrown
                }
            }
        }
        return params;
    }
}