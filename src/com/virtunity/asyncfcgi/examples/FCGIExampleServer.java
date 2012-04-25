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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.virtunity.asyncfcgi.common.FCGICodecFactory;
import com.virtunity.asyncfcgi.common.FCGIRequest;
import com.virtunity.asyncfcgi.common.FCGIResponse;

/**
 * Example FastCGI server.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com
 */
public class FCGIExampleServer extends IoHandlerAdapter {
    private NioSocketAcceptor acceptor = null;
    private Executor thread_pool = Executors.newCachedThreadPool();
    
    /**
     * Example main method.
     */
    public static void main(String[] args) {
        new FCGIExampleServer(1025);
    }
    
    /**
     * Create new server instance.
     * 
     * @param port Server port
     */
    public FCGIExampleServer(int port) {
        this.acceptor = new NioSocketAcceptor();
        this.acceptor.setDefaultLocalAddress(new InetSocketAddress(port));
        this.acceptor.setHandler(this);
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new FCGICodecFactory(FCGIRequest.class)));
        this.acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(thread_pool));
        this.acceptor.setBacklog(1000);
        try {
            this.acceptor.bind();
            System.out.println("Server started...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Called when a message is received.
     * 
     * @param session The underlying session
     * @param message The received message (or request or response)
     */
    public void messageReceived(IoSession session, Object message) {
        // We receive request objects (see codec factory setting)
        FCGIRequest request = (FCGIRequest) message;
        System.out.print("Received "); request.printDebug();
        
        // Create and send a response object
        FCGIResponse response = new FCGIResponse();
        response.setId(request.getId());
        String stdout = "Content-Type: text/html\n\nHello World!";
        response.setStdout(IoBuffer.wrap(stdout.getBytes()));
        String stderr = "An imaginary error occured (not really)";
        response.setStderr(IoBuffer.wrap(stderr.getBytes()));
        response.setApplicationStatus(1234);
        session.write(response);
        System.out.print("Sent "); response.printDebug();
        session.close(false); // closeOnFlush(); // deprecated in Mina 2.0.0.M4
    }
    
    /**
     * Called when an exception is caught.
     * 
     * @param session The underlying session
     * @param cause The caught exception
     */
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        System.out.println("Exception caught ("+session+"): "+cause);
        if (session.isConnected()) {
            session.close();
        }
    }
}
