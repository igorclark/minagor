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
import com.virtunity.asyncfcgi.common.FCGIMessage;
import com.virtunity.asyncfcgi.common.FCGIMessageType;
import com.virtunity.asyncfcgi.common.FCGIProtocolStatus;
import com.virtunity.asyncfcgi.common.FCGIRequest;

/**
 * Example FastCGI server using low level messages instead of responses.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com
 */
public class FCGIExampleLowLevelServer extends IoHandlerAdapter {
    private NioSocketAcceptor acceptor = null;
    private Executor thread_pool = Executors.newCachedThreadPool();
    
    /**
     * Example main method.
     */
    public static void main(String[] args) {
        new FCGIExampleLowLevelServer(1025);
    }
    
    /**
     * Create new example server instance.
     * 
     * @param port Server port
     */
    public FCGIExampleLowLevelServer(int port) {
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
        // We receive higher level request objects
        FCGIRequest request = (FCGIRequest) message;
        System.out.print("Received "); request.printDebug();
        
        // Create a low level response consisting of multiple low level messages
        FCGIMessage outmsg = new FCGIMessage(FCGIMessageType.STDOUT);
        outmsg.setRequestId(request.getId());

        // Send first part
        outmsg.setContent(IoBuffer.wrap("One".getBytes()));
        session.write(outmsg);
        System.out.print("Sending "); outmsg.printDebug();
        
        // Send second part
        outmsg.setContent(IoBuffer.wrap("Two".getBytes()));
        session.write(outmsg);
        System.out.print("Sending "); outmsg.printDebug();
        
        // Send third part
        outmsg.setContent(IoBuffer.wrap("Three".getBytes()));
        session.write(outmsg);
        System.out.print("Sending "); outmsg.printDebug();
        
        // Send stdout end message (just an empty message of type STDOUT)
        outmsg.setContent(null);
        session.write(outmsg);
        System.out.print("Sending "); outmsg.printDebug();
        
        // Send end request (see fastcgi protocol specs for more details)
        outmsg.setType(FCGIMessageType.END_REQUEST);
        IoBuffer content = IoBuffer.allocate(8);
        content.clear();
        content.putInt(0); // Application status
        content.put((byte) FCGIProtocolStatus.REQUEST_COMPLETE.getValue());
        for (int i=0; i<3; i++) content.put((byte) 0); // Three reserved bytes
        outmsg.setContent(content);
        session.write(outmsg);
        System.out.print("Sending "); outmsg.printDebug();
        
        // Close session on flush
        session.close(false); // closeOnFlush(); // deprecated in Mina 2.0.0.M4
    }
    
    /**
     * Called when an exception was caught.
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
