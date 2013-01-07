/**
 * 
 */
package org.snova.c4.server.session.v3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.arch.common.KeyValuePair;
import org.arch.event.Event;
import org.arch.event.TypeVersion;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.snova.c4.common.C4Constants;
import org.snova.c4.common.event.SocketConnectionEvent;
import org.snova.c4.common.event.SocketReadEvent;
import org.snova.c4.common.event.TCPChunkEvent;

/**
 * @author wqy
 * 
 */
public class RemoteProxySession
{
	String user;
	int groupIndex;
	int sid;
	private int sequence;
	private String remoteAddr;
	SocketChannel client = null;
	SelectionKey key;
	int ops;
	private boolean isHttps;
	private ByteBuffer buffer = ByteBuffer.allocate(65536);
	private byte[] httpRequestContent = null;

	private RemoteProxySessionManager sessionManager = null;

	public RemoteProxySession(RemoteProxySessionManager sessionManager,
	        String user, int groupIdx, int sid)
	{
		this.sessionManager = sessionManager;
		groupIndex = groupIdx;
		this.sid = sid;
		this.user = user;
	}

	void close()
	{
		doClose(key, client, remoteAddr);
	}

	protected static byte[] buildRequestContent(HTTPRequestEvent ev)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(ev.method).append(" ").append(ev.url).append(" ")
		        .append("HTTP/1.1\r\n");
		for (KeyValuePair<String, String> header : ev.headers)
		{
			buffer.append(header.getName()).append(":")
			        .append(header.getValue()).append("\r\n");
		}
		buffer.append("\r\n");
		byte[] header = buffer.toString().getBytes();
		if (ev.content.readable())
		{
			byte[] all = new byte[header.length + ev.content.readableBytes()];
			System.arraycopy(header, 0, all, 0, header.length);
			ev.content.read(all, header.length, ev.content.readableBytes());
			return all;
		}
		else
		{
			return header;
		}
	}

	private boolean writeContent(byte[] content)
	{
		if (null != client)
		{
			try
			{
				int n = client.write(ByteBuffer.wrap(content));
				if (n < content.length)
				{
					System.out.println("###########Not finish write.");
				}
				return true;
			}
			catch (IOException e)
			{

				return false;
			}
		}
		return false;
	}

	void resume()
	{
		if (null == client)
		{
			return;
		}
		sessionManager.addInvokcation(new Runnable()
		{
			public void run()
			{
				try
				{
					if (null != client)
					{
						ops = ops | SelectionKey.OP_READ;
						key = client.register(sessionManager.selector, ops,
						        new SessionAddressPair(RemoteProxySession.this,
						                remoteAddr));
					}
				}
				catch (ClosedChannelException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	void pause()
	{
		if (null == client)
		{
			return;
		}
		sessionManager.addInvokcation(new Runnable()
		{
			public void run()
			{
				try
				{
					if (null != client)
					{
						ops = ops & ~SelectionKey.OP_READ;
						key = client.register(sessionManager.selector, ops,
						        new SessionAddressPair(RemoteProxySession.this,
						                remoteAddr));
					}
				}
				catch (ClosedChannelException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	boolean handleEvent(TypeVersion tv, Event ev)
	{
		switch (tv.type)
		{
			case C4Constants.EVENT_TCP_CHUNK_TYPE:
			{
				// System.out.println("#####recv chunk for " + remoteAddr);
				if (null != client)
				{
					final TCPChunkEvent chunk = (TCPChunkEvent) ev;
					return writeContent(chunk.content);
				}
				else
				{
					return false;
				}
			}
			case C4Constants.EVENT_SOCKET_READ_TYPE:
			{
				SocketReadEvent event = (SocketReadEvent) ev;
				// return readClient(event.maxread, event.timeout);
				break;
			}
			case C4Constants.EVENT_TCP_CONNECTION_TYPE:
			{
				SocketConnectionEvent event = (SocketConnectionEvent) ev;
				if (event.status == SocketConnectionEvent.TCP_CONN_CLOSED)
				{
					doClose(key, client, remoteAddr);
				}
				break;
			}
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				HTTPRequestEvent req = (HTTPRequestEvent) ev;
				String host = req.getHeader("Host");
				int port = 80;
				if (host.indexOf(":") != -1)
				{
					String[] ss = host.split(":");
					host = ss[0];
					port = Integer.parseInt(ss[1]);
				}
				else
				{
					if (req.method.equalsIgnoreCase("Connect"))
					{
						port = 443;
					}
				}
				if (req.method.equalsIgnoreCase("Connect"))
				{
					isHttps = true;
				}
				else
				{
					httpRequestContent = buildRequestContent(req);
				}
				// System.out.println("Session[" + sid + "] rec req " + req);
				if (checkClient(host, port))
				{
					if (null != httpRequestContent)
					{
						writeContent(httpRequestContent);
						httpRequestContent = null;
					}
				}
				break;
			}
			default:
			{
				// logger.error("Unsupported event type version " + tv.type +
				// ":"
				// + tv.version);
				return false;
			}
		}
		return true;
	}

	void onConnected()
	{
		System.out.println("#####" + remoteAddr + " connected!");
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try
		{
			if (socketChannel.isConnectionPending())
			{
				socketChannel.finishConnect();
			}
			ops = SelectionKey.OP_READ;
			key = socketChannel.register(sessionManager.selector, ops,
			        new SessionAddressPair(this, remoteAddr));
			if (isHttps)
			{
				TCPChunkEvent chunk = new TCPChunkEvent();
				chunk.sequence = sequence;
				sequence++;
				chunk.setHash(sid);
				chunk.content = "HTTP/1.1 200 OK\r\n\r\n".getBytes();
				sessionManager.offerReadyEvent(user, groupIndex, chunk);
			}
			else
			{
				writeContent(httpRequestContent);
				httpRequestContent = null;
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (isHttps)
			{
				TCPChunkEvent chunk = new TCPChunkEvent();
				chunk.sequence = sequence;
				sequence++;
				chunk.setHash(sid);
				chunk.content = "HTTP/1.1 503 Service Unavailable\r\n\r\n"
				        .getBytes();
				sessionManager.offerReadyEvent(user, groupIndex, chunk);
			}
			doClose(key, client, remoteAddr);
		}
	}

	void onRead(SelectionKey key, String address)
	{
		buffer.clear();
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try
		{
			int n = socketChannel.read(buffer);
			// System.out.println("#####" + remoteAddr + " onread:" + n);
			if (n < 0)
			{
				doClose(key, socketChannel, address);
			}
			else if (n > 0)
			{
				buffer.flip();
				TCPChunkEvent chunk = new TCPChunkEvent();
				chunk.sequence = sequence;
				sequence++;
				chunk.setHash(sid);
				chunk.content = new byte[n];
				buffer.get(chunk.content);
				if (!sessionManager.offerReadyEvent(user, groupIndex, chunk))
				{
					System.out.println("#############pause since busy!");
					pause();
				}
			}
			else
			{
				System.out.println("#############read 0 for " + sid + " :"
				        + remoteAddr);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("#####Close " + address);
			doClose(key, socketChannel, address);
		}
	}

	private void doClose(SelectionKey key, SocketChannel channel, String address)
	{
		if (null == address)
		{
			return;
		}
		if (null != channel)
		{
			try
			{
				channel.close();
				key.cancel();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (address.equals(remoteAddr))
		{
			SocketConnectionEvent closeEv = new SocketConnectionEvent();
			closeEv.setHash(sid);
			closeEv.addr = remoteAddr;
			closeEv.status = SocketConnectionEvent.TCP_CONN_CLOSED;
			sessionManager.offerReadyEvent(user, groupIndex, closeEv);
			client = null;
		}
		// if (null != key)
		// {
		// sessionManager.addInvokcation(new Runnable()
		// {
		// public void run()
		// {
		// key.cancel();
		// key = null;
		// }
		// });
		// }
	}

	private boolean checkClient(String host, int port)
	{
		String addr = host + ":" + port;
		if (addr.equals(remoteAddr) && null != client && client.isConnected())
		{
			return true;
		}
		String oldAddr = remoteAddr;
		remoteAddr = addr;
		doClose(key, client, oldAddr);
		try
		{
			remoteAddr = addr;
			client = SocketChannel.open();
			client.configureBlocking(false);
			client.connect(new InetSocketAddress(host, port));
			sessionManager.addInvokcation(new Runnable()
			{
				public void run()
				{
					try
					{
						ops = SelectionKey.OP_CONNECT;
						key = client.register(sessionManager.selector, ops,
						        new SessionAddressPair(RemoteProxySession.this,
						                remoteAddr));
					}
					catch (ClosedChannelException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return false;
	}
}
