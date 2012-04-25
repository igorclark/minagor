package net.igorclark.minagor;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.virtunity.asyncfcgi.common.FCGICodecFactory;
import com.virtunity.asyncfcgi.common.FCGIRequest;

public class MinaGor
{
    private static final int PORT = 1025;

    public static void main(String[] args) throws IOException
    {
    	NioSocketAcceptor acceptor = new NioSocketAcceptor();
    	
        //acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new FCGICodecFactory(FCGIRequest.class)));

        acceptor.setDefaultLocalAddress(new InetSocketAddress(PORT));
        acceptor.setHandler(new MinaGorHandler());
        acceptor.setBacklog(1000);

//        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        
        try {
            acceptor.bind();
            System.out.println("Server started on port " + PORT);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}