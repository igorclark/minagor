package net.igorclark.minagor;

import net.igorclark.minagor.application.Cycle;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.virtunity.asyncfcgi.common.FCGIRequest;
import com.virtunity.asyncfcgi.common.FCGIResponse;

public class MinaGorHandler extends IoHandlerAdapter {

    @Override
    public void exceptionCaught( IoSession ioSession, Throwable cause ) throws Exception
    {
        cause.printStackTrace();
        if (ioSession.isConnected()) {
        	ioSession.close(false);// close(); // deprecated in Mina 2.0.0.M4
        }
    }

    @Override
    public void messageReceived( IoSession ioSession, Object message ) throws Exception
    {
    	FCGIRequest		fcgiRequest		= (FCGIRequest)message;
        FCGIResponse	fcgiResponse	= new FCGIResponse(fcgiRequest.getId());

        Cycle			cycle			= new Cycle(fcgiRequest, fcgiResponse);
		cycle.process();
		
        ioSession.write(fcgiResponse);
        ioSession.close(false);// closeOnFlush(); // deprecated in Mina 2.0.0.M4
    }

    @Override
    public void sessionIdle( IoSession ioSession, IdleStatus status ) throws Exception
    {
        System.out.println( "IDLE " + ioSession.getIdleCount( status ));
    }
}
