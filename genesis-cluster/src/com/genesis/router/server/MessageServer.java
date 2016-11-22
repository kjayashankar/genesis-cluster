/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.genesis.router.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.helper.GlobalInit;
import com.genesis.monitors.Flooder;
import com.genesis.monitors.NetworkMonitor;
import com.genesis.monitors.QueueMonitor;
import com.genesis.monitors.StealerThread;
import com.genesis.queues.workers.GlobalOutWorker;
import com.genesis.queues.workers.ThreadPool;
import com.genesis.router.container.GlobalConf;
import com.genesis.router.container.RoutingConf;
import com.genesis.router.server.edges.EdgeMonitor;
import com.genesis.router.server.edges.GlobalEdgeMonitor;
import com.genesis.router.server.tasks.SimpleBalancer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MessageServer {
	protected static Logger logger = LoggerFactory.getLogger("server");

	protected static HashMap<Integer, ServerBootstrap> bootstrap = new HashMap<Integer, ServerBootstrap>();

	// public static final String sPort = "port";
	// public static final String sPoolSize = "pool.size";

	protected RoutingConf conf;
	protected GlobalConf globalConf;

	protected boolean background = false;
	private static ServerState state;
	/**
	 * initialize the server with a configuration of it's resources
	 * 
	 * @param cfg
	 */
	public MessageServer(File cfg, File gfg) {
		init(cfg,gfg);
	}

	/**
	 * Initializes state as it will be needed in both worker and command threads
	 */
	
	public void initServer(){
		//Initializing values in the init method
				state = new ServerState();
				state.setConf(conf);
				
				NetworkMonitor nmon = NetworkMonitor.getInstance();
				nmon.setState(state);
				state.setNetworkmon(nmon);
				
				QueueMonitor qMon = new QueueMonitor(conf.getWorkerThreads(),state,new SimpleBalancer());
				state.setQueueMonitor(qMon);
				
				
	}
	
	public MessageServer(RoutingConf conf) {
		this.conf = conf;
	}

	public void release() {
	}

	public void startServer() {
		//Initializing the server's state ...
		initServer();
		StartWorkCommunication comm = new StartWorkCommunication(conf);
		logger.info("Work starting");
		// We always start the worker in the background
		Thread cthread = new Thread(comm);
		cthread.start();
		state.setGlobalConf(globalConf);
		
		
		GlobalOutWorker worker = new GlobalOutWorker(state);
		worker.start();
		
		StartGlobalCommunication global = new StartGlobalCommunication(globalConf,state);
		Thread globalThread = new Thread(global);
		globalThread.start();
		
		StartCommandCommunication comm2 = new StartCommandCommunication(conf);
		logger.info("Command starting");
		if (background) {
			Thread cthread2 = new Thread(comm2);
			cthread2.start();
		} else {
			comm2.run();
		}
		
			
	}
	
		private static class StartGlobalCommunication implements Runnable{
			
			public ServerState state;
			public GlobalConf globalConf;
			public GlobalEdgeMonitor gMon = null;
			
			
			public StartGlobalCommunication(GlobalConf conf,ServerState state){
				this.state = state;
				this.globalConf = conf;
				gMon = new GlobalEdgeMonitor(state);
				state.setGlobalMonitor(gMon);
			}
			
			public void run() {
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();

				try {
					ServerBootstrap b = new ServerBootstrap();
					bootstrap.put(globalConf.getGlobalPort(), b);

					b.group(bossGroup, workerGroup);
					b.channel(NioServerSocketChannel.class);
					
					b.option(ChannelOption.SO_BACKLOG, 100);
					b.option(ChannelOption.TCP_NODELAY, true);
					b.option(ChannelOption.SO_KEEPALIVE, true);

					boolean compressComm = false;
					b.childHandler(new GlobalInit(state, compressComm));

					// Start the server.
					logger.info("Starting global server (" + globalConf.getClusterId() + "), listening on port = "
							+ globalConf.getGlobalPort());
					ChannelFuture f = b.bind(globalConf.getGlobalPort()).syncUninterruptibly();

					logger.info(f.channel().localAddress() + " -> open: " + f.channel().isOpen() + ", write: "
							+ f.channel().isWritable() + ", act: " + f.channel().isActive());

					// block until the server socket is closed.
					f.channel().closeFuture().sync();

				} catch (Exception ex) {
					// on bind().sync()
					logger.error("Failed to setup handler.", ex);
				} finally {
					// Shut down all event loops to terminate all threads.
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
				
			}
		}

	/**
	 * static because we need to get a handle to the factory from the shutdown
	 * resource
	 */
	public static void shutdown() {
		logger.info("Server shutdown");
		System.exit(0);
	}

	private void init(File cfg,File gfg) {
		if (!cfg.exists())
			throw new RuntimeException(cfg.getAbsolutePath() + " not found");
		// resource initialization - how message are processed
		BufferedInputStream br = null;
		try {
			byte[] raw = new byte[(int) cfg.length()];
			br = new BufferedInputStream(new FileInputStream(cfg));
			br.read(raw);
			conf = JsonUtil.decode(new String(raw), RoutingConf.class);
			if (!verifyConf(conf))
				throw new RuntimeException("verification of configuration failed");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		// global file configuration
		if (!gfg.exists())
			throw new RuntimeException(gfg.getAbsolutePath() + " not found");
		// resource initialization - how message are processed
		BufferedInputStream br2 = null;
		try {
			byte[] raw = new byte[(int) gfg.length()];
			br2 = new BufferedInputStream(new FileInputStream(gfg));
			br2.read(raw);
			globalConf = JsonUtil.decode(new String(raw), GlobalConf.class);
			if( globalConf == null )
				throw new RuntimeException("verification of global configuration failed");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (br2 != null) {
				try {
					br2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean verifyConf(RoutingConf conf) {
		return (conf != null);
	}

	/**
	 * initialize netty communication
	 * 
	 * @param port
	 *            The port to listen to
	 */
	private static class StartCommandCommunication implements Runnable {
		RoutingConf conf;

		public StartCommandCommunication(RoutingConf conf) {
			this.conf = conf;
		}

		public void run() {
			// construct boss and worker threads (num threads = number of cores)

			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				ServerBootstrap b = new ServerBootstrap();
				bootstrap.put(conf.getCommandPort(), b);

				b.group(bossGroup, workerGroup);
				b.channel(NioServerSocketChannel.class);
				b.option(ChannelOption.SO_BACKLOG, 100);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);
				// b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR);

				boolean compressComm = false;
				b.childHandler(new CommandInit(state, compressComm));

				// Start the server.
				logger.info("Starting command server (" + conf.getNodeId() + "), listening on port = "
						+ conf.getCommandPort());
				ChannelFuture f = b.bind(conf.getCommandPort()).syncUninterruptibly();

				logger.info(f.channel().localAddress() + " -> open: " + f.channel().isOpen() + ", write: "
						+ f.channel().isWritable() + ", act: " + f.channel().isActive());

				// block until the server socket is closed.
				f.channel().closeFuture().sync();

			} catch (Exception ex) {
				// on bind().sync()
				logger.error("Failed to setup handler.", ex);
			} finally {
				// Shut down all event loops to terminate all threads.
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}
	}

	/**
	 * initialize netty communication
	 * 
	 * @param port
	 *            The port to listen to
	 */
	private static class StartWorkCommunication implements Runnable {
		

		public StartWorkCommunication(RoutingConf conf) {
			if (conf == null)
				throw new RuntimeException("missing conf");

			
			
			
			
			EdgeMonitor emon = new EdgeMonitor(state);
			Thread t = new Thread(emon);
			t.start();
			
			/*GlobalThread global = new GlobalThread();
			global.setAddress("169.254.244.97",4567 );
			global.start();*/
		
			Flooder dT = new Flooder();
			dT.setState(state);
			new Thread(dT).start();
			
			
			StealerThread stealer = new StealerThread(state);
			stealer.start();
			
			ThreadPool pool = new ThreadPool(conf.getWorkerThreads(), state);
			pool.setThreadPause(1000);
			pool.init();
			pool.startWorkers(); 
		}

		public void run() {
			// construct boss and worker threads (num threads = number of cores)

			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				ServerBootstrap b = new ServerBootstrap();
				bootstrap.put(state.getConf().getWorkPort(), b);

				b.group(bossGroup, workerGroup);
				b.channel(NioServerSocketChannel.class);
				b.option(ChannelOption.SO_BACKLOG, 100);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);
				// b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR);

				boolean compressComm = false;
				b.childHandler(new WorkInit(state, compressComm));

				// Start the server.
				logger.info("Starting work server (" + state.getConf().getNodeId() + "), listening on port = "
						+ state.getConf().getWorkPort());
				ChannelFuture f = b.bind(state.getConf().getWorkPort()).syncUninterruptibly();

				logger.info(f.channel().localAddress() + " -> open: " + f.channel().isOpen() + ", write: "
						+ f.channel().isWritable() + ", act: " + f.channel().isActive());

				// block until the server socket is closed.
				f.channel().closeFuture().sync();

			} catch (Exception ex) {
				// on bind().sync()
				logger.error("Failed to setup handler.", ex);
			} finally {
				// Shut down all event loops to terminate all threads.
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();

				// shutdown monitor
				EdgeMonitor emon = state.getEmon();
				if (emon != null)
					emon.shutdown();
			}
		}
	}

	/**
	 * help with processing the configuration information
	 * 
	 * @author gash
	 *
	 */
	public static class JsonUtil {
		private static JsonUtil instance;

		public static void init(File cfg) {

		}

		public static JsonUtil getInstance() {
			if (instance == null)
				throw new RuntimeException("Server has not been initialized");

			return instance;
		}

		public static String encode(Object data) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(data);
			} catch (Exception ex) {
				return null;
			}
		}

		public static <T> T decode(String data, Class<T> theClass) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(data.getBytes(), theClass);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}

}
