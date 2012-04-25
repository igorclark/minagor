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

package com.virtunity.asyncfcgi.examples;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.virtunity.asyncfcgi.common.FCGICodecFactory;
import com.virtunity.asyncfcgi.common.FCGIRequest;
import com.virtunity.asyncfcgi.common.FCGIResponse;

/**
 * FastCGI IoHandler example.
 *
 * Example one-shot client implementation of the FastCGI protocol on top of Mina.
 * You require a working FastCGI backend:
 * To spawn a FastCGI listener run "php-cgi -b 1025" or use the provided server
 * example.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIExampleHandler extends IoHandlerAdapter {
    private String document_root = "D:/virtunity/appserver/AppServer/web";
    private String path = "/phpinfo.php";
    
    /**
     * Example main method.
     */
    public static void main(String[] args) {
        // Create a new connector and set it up
        NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeout(3);
        connector.setHandler(new FCGIExampleHandler());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new FCGICodecFactory(FCGIResponse.class)));
        // Connect to the fastcgi backend
        connector.connect(new InetSocketAddress("127.0.0.1", 1025));
        System.out.println("Connecting...");
        // And now wait for sessionCreated (see below)
    }
    
    /**
     * Called when a message has been sent to the socket.
     * 
     * @param session The underlying session
     * @param message The sent message (or request or response)
     */
    public void messageSent(IoSession session, Object message) {
        // We use requests in here...
        FCGIRequest request = (FCGIRequest) message;
        System.out.print("Sent "); request.printDebug();
    }
    
    /**
     * Called when a message is received.
     * 
     * @param session The underlying session
     * @param message The received message (or request or response)
     */
    public void messageReceived(IoSession session, Object message) {
        // We expect a response in here (see codec factory settings above)
        FCGIResponse response = (FCGIResponse) message;
        System.out.print("Received "); response.printDebug();
        if (response.getStdout() != null) {
            String stdout = new String(response.getStdout().array());
            System.out.println("Stdout: "+stdout);
        }
        if (response.getStderr() != null) {
            String stderr = new String(response.getStderr().array());
            System.out.println("Stderr: "+stderr);
        }
        session.close();
        // It is required to close the session afterwards since multiplexed
        // connections are neigher implemented nor supported e.g. by PHP.
    }
    
    /**
     * Called when an exception occured while sending the message.
     * 
     * @param session The underlying session
     * @param ex The caught exception
     */
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (session.isConnected()) {
            session.close();
        }
    }
    
    /**
     * Called when a new session is created.
     * 
     * @param session The created session.
     */
    public void sessionCreated(IoSession session) {
        // Since this is just an example, we construct the request object
        // here and send it to the session.
        FCGIRequest request = new FCGIRequest();
        
        // Set request ID (else it will be automatically generated)
        request.setId(1);

        // There are much more params to take care of, but these are enough
        // for testing (at least when using PHP).
        Map<String,String> params = new HashMap();
        params.put("SCRIPT_FILENAME", document_root + path);
        request.setParams(params);
        request.setStdin(null); // No POST data, let's assume this is a GET request.
        
        session.write(request);
    }
}