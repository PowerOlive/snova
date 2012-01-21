/**
 * 
 */
package org.snova.framework.httpserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.arch.util.NetworkHelper;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.config.SimpleSocketAddress;

/**
 * @author yinqiwen
 * 
 */
public class HttpLocalProxyServer
{
	protected static Logger	logger	     = LoggerFactory
	                                             .getLogger(HttpLocalProxyServer.class);
	
	// private List<RpcChannel> rpcChannels = new LinkedList<RpcChannel>();
	private ServerBootstrap	bootstrap;
	// private final ThreadPoolExecutor workerExecutor;
	private Channel	        serverChannel;
	private Executor	    bossExecutor	= Executors.newCachedThreadPool();
	
	public HttpLocalProxyServer(SimpleSocketAddress listenAddress,
	        final ExecutorService workerExecutor)
	{
		String host = listenAddress.host;
		int port = listenAddress.port;
		
		if (NetworkHelper.isIPV6Address(host))
		{
			bootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(
			        bossExecutor, workerExecutor));
		}
		else
		{
			bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		}
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("executor", new ExecutionHandler(
				        workerExecutor));
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				// pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
				pipeline.addLast("handler", new HttpLocalProxyRequestHandler());
				return pipeline;
			}
		});
		serverChannel = bootstrap.bind(new InetSocketAddress(host, port));
	}
	
	public void close()
	{
		serverChannel.close();
	}
	
}
