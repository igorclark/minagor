package net.igorclark.minagor.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedClient;

public class MemcachedSessionStore extends SessionStore {
	
	MemcachedClient mc;
	public static final String	HOST			= "localhost";
	public static final int		PORT			= 11211;
	public static final String	MEMCACHED_KEY	= "minagor-session-";
	
	/*
	// Here's how to get a memcached client connected to several servers
	MemcachedClient c=new MemcachedClient(AddrUtil.getAddresses("server1:11211 server2:11211"));
	*/
	
	public MemcachedSessionStore() throws IOException {
		super();
		/* simple 1-host constructor */
		//mc = new MemcachedClient(new InetSocketAddress(HOST, PORT));

		/*
		 * Multi-host client using Ketama hashing
		 */
		ConnectionFactory connFactory = new DefaultConnectionFactory(
			DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN,
			DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE,
			HashAlgorithm.KETAMA_HASH
		);

		List<InetSocketAddress> addrs = AddrUtil.getAddresses("server1:11211 server2:11211");
		mc = new MemcachedClient(connFactory, addrs);
	}
	
	public	Session getSession(String id) {
		System.out.println("Asking memcached for session with id " + id);

		// Try to get a value, for up to 5 seconds, and cancel if it doesn't return
		Session s = null;
		Future<Object> f = mc.asyncGet(MEMCACHED_KEY + id);
		try {
			s = (Session)f.get(5, TimeUnit.SECONDS);
		} catch(TimeoutException e) {
			// Since we don't need this, go ahead and cancel the operation.  This
			// is not strictly necessary, but it'll save some work on the server.
			f.cancel(false);
			// Do other timeout related stuff
			e.printStackTrace();
		} catch (InterruptedException e) {
			f.cancel(false);
			e.printStackTrace();
		} catch (ExecutionException e) {
			f.cancel(false);
			e.printStackTrace();
		}
		
		if(null == s) {
			System.out.println("Sesion id " + id + " is not in memcached, creating.");
			s = new Session(id);
			id = s.id();
			mc.set(MEMCACHED_KEY + s.id(), 3600, s);
		}

		System.out.println("Got session id " + s.id() + ": " + s.toString());
		return s;
	}
	
	public void saveSession(Session s) {
		mc.set(MEMCACHED_KEY + s.id(), 3600, s);
	}
}
