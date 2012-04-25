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
import java.util.Iterator;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * FastCGI ProtocolEncoder.
 *
 * Encodes {@link FCGIMessage}, {@link FCGIRequest} or {@link FCGIResponse} objects
 * into an {@link IoBuffer} for further processing.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIEncoder implements ProtocolEncoder {
    
    /**
     * Create new encoder instance.
     */
    public FCGIEncoder() {
    }
    
    public void dispose(IoSession session) {
    }
    
    /**
     * Encode {@link FCGIRequest}, {@link FCGIResponse} or {@link FCGIMessage} to
     * an {@link IoBuffer} object for further processing.
     */
    public void encode(IoSession session, Object message, ProtocolEncoderOutput output) {
        IoBuffer buffer = IoBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.setAutoExpand(true);
        buffer.clear();
        
        // Encode whole request into several messages
        if (message instanceof FCGIRequest) {
            FCGIRequest request = (FCGIRequest) message;
            int id = request.getId();
            
            // Create begin request
            writeHeader(buffer, FCGIMessageType.BEGIN_REQUEST.getValue(), id, 8, 0);
            buffer.putShort((short) request.getRole().getValue());
            buffer.put((byte) request.getFlags());
            for (int i=3; i<8; i++) buffer.put((byte) 0); // Reserved
            
            // Create params (we need a temporary parameters buffer to catch its length
            // and to split it up optionally.
            if (request.getParams().size() > 0) {
                IoBuffer pbuf = makeParams(request.getParams());
                writeSplitMessage(buffer, FCGIMessageType.PARAMS.getValue(), id, pbuf.flip());
            }
            writeHeader(buffer, FCGIMessageType.PARAMS.getValue(), id, 0, 0); // Params complete
            
            // Send standard input and split it up if required
            IoBuffer stdin = request.getStdin();
            if (stdin != null) {
                // Conditionally flip
                if (stdin.position() > 0) {
                    stdin.flip();
                }
                if (stdin.remaining() > 0) writeSplitMessage(buffer, FCGIMessageType.STDIN.getValue(), id, stdin);
            }
            writeHeader(buffer, FCGIMessageType.STDIN.getValue(), id, 0, 0); // Stdin complete
            
        // Encode whole response into several packages
        } else if (message instanceof FCGIResponse) {
            FCGIResponse response = (FCGIResponse) message;
            int id = response.getId();
            
            // Send standard output and split it up if required
            IoBuffer stdout = response.getStdout();
            if (stdout != null) {
                // Conditionally flip
                if (stdout.position() > 0) {
                    stdout.flip();
                }
                if (stdout.remaining() > 0) writeSplitMessage(buffer, FCGIMessageType.STDOUT.getValue(), id, stdout);
            }
            writeHeader(buffer, FCGIMessageType.STDOUT.getValue(), id, 0, 0); // Stdout complete
            
            // Send optional standard error and split it up if required
            IoBuffer stderr = response.getStderr();
            if (stderr != null) {
                // Conditionally flip
                if (stderr.position() > 0) {
                    stderr.flip();
                }
                if (stderr.remaining() > 0) {
                    writeSplitMessage(buffer, FCGIMessageType.STDERR.getValue(), id, stderr);
                    writeHeader(buffer, FCGIMessageType.STDERR.getValue(), id, 0, 0); // Stderr complete (if present)
                }
            }
            
            // Send end request
            writeHeader(buffer, FCGIMessageType.END_REQUEST.getValue(), id, 8, 0);
            buffer.putInt(response.getApplicationStatus());
            buffer.put((byte) response.getProtocolStatus().getValue());
            for (int i=5; i<8; i++) buffer.put((byte) 0); // Reserved
            
        // Encode custom message
        } else if (message instanceof FCGIMessage) {
            FCGIMessage fcgimessage = (FCGIMessage) message;
            
            if (fcgimessage.getContent() != null) {
                // Custom messages normally are of a maximum length but to make life as
                // easy and secure as possible they will be splitted if neccessary.
                IoBuffer content = (fcgimessage.getContent().position() > 0) ?  fcgimessage.getContent().flip() : fcgimessage.getContent();
                writeSplitMessage(buffer, fcgimessage.getType().getValue(), fcgimessage.getRequestId(), content);
            } else {
                writeHeader(buffer, fcgimessage.getType().getValue(), fcgimessage.getRequestId(), 0, 0);
            }
        
        // Else throw exception (how do I throw it correctly?)
        } else {
            throw(new ClassCastException("Incompatible message type"));
        }
        
        // Finally write buffer
        buffer.flip();
        output.write(buffer);
    }
    
    /**
     * Write raw FastCGI header.
     */
    private static void writeHeader(IoBuffer buffer, int type, int id, int content_length, int padding_length) {
        buffer.put((byte) 1);
        buffer.put((byte) type);
        buffer.putShort((short) id);
        buffer.putShort((short) content_length);
        buffer.put((byte) padding_length);
        buffer.put((byte) 0); // Reserved
    }
    
    /**
     * Write raw FastCGI header and correctly split content into multiple messages
     * if neccessary.
     */
    private static void writeSplitMessage(IoBuffer buffer, int type, int id, IoBuffer content) {
        int pos = 0;
        
        int max = FCGIMessage.MAXLEN - FCGIMessage.HEADERLEN;
        while (content.remaining() > 0) {
            int content_length = content.remaining();
            int reallimit = content.limit();
            if (content_length > max) {
                content.limit(content.position() + max);
                content_length = max;
            }
            int padding_length = 8 - content_length % 8;
            writeHeader(buffer, type, id, content_length, padding_length);
            buffer.put(content);
            for (int i=0; i<padding_length; i++) buffer.put((byte) 'x');
            content.limit(reallimit);
        }
    }
    
    /**
     * Make raw parameters content.
     */
    private static IoBuffer makeParams(Map<String,String> params) {
        IoBuffer buffer = IoBuffer.allocate(2048);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.setAutoExpand(true);
        buffer.clear();
        Iterator<String> i = params.keySet().iterator();
        while (i.hasNext()) {
            try {
                // Convert strings to byte arrays to get the correct length
                String name = i.next();
                String value = params.get(name);
                byte[] nb = name.getBytes("UTF-8");
                byte[] vb = (value != null) ? value.getBytes("UTF-8") : new byte[0];
                int name_length = nb.length;
                int value_length = vb.length;
                // For more information about conditional lengths see FCGIDecoder.readParams
                if (name_length < 0x80) {
                    buffer.put((byte) name_length);
                } else {
                    buffer.putInt(name_length | (0x80 << 24));
                }
                if (value_length < 0x80) {
                    buffer.put((byte) value_length);
                } else {
                    buffer.putInt(value_length | (0x80 << 24));
                }
                buffer.put(nb);
                buffer.put(vb);
            } catch (UnsupportedEncodingException ex) {
                // UTF-8 -> Never thrown
            }
        }
        return buffer;
    }
}