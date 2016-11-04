package com.genesis.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.container.RoutingConf.RoutingEntry;
import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeList;
import com.genesis.router.server.tasks.SimpleBalancer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Failure;
import pipe.common.Common.Header;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;

public class WorkStealerHandler extends SimpleChannelInboundHandler<WorkMessage> {
	protected static Logger logger = LoggerFactory.getLogger("NewWorkChannelHandler");
	protected ServerState state;
	protected boolean debug = true;
	//start of message handlers chain 
	
	
	private EdgeList outboundEdges;
	private EdgeList inboundEdges;
	private boolean forever = true;
	
	

	public WorkStealerHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
		this.outboundEdges = new EdgeList();
		this.inboundEdges = new EdgeList();
		
		
		if (state.getConf().getRouting() != null) {
			for (RoutingEntry e : state.getConf().getRouting()) {
				outboundEdges.addNode(e.getId(), e.getHost(), e.getPort());
			}
		}
	}
	
	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 */
	
	public void handleMessage(WorkMessage msg, Channel channel) {
		Task returnTask= null;
		WorkMessage wm = null;
		if (msg == null) {
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}
		
		logger.info("Response sent back from " + msg.getHeader().getNodeId());

		try {
			if (msg.hasSteal()) {
				returnTask = stolenTask();
				if(returnTask!=null){
					wm = createTaskResponse( returnTask);
					channel.writeAndFlush(wm);
				}
				else{
					wm = createTaskReturnFailureResponse( returnTask, msg);
					channel.writeAndFlush(wm);
				}
				logger.info("Response sent back from " + msg.getHeader().getNodeId());
			}
		} catch (Exception e) {
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			eb.setMessage(e.getMessage());
			WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
		}

		System.out.flush();

	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}
	
	private WorkMessage createTaskReturnFailureResponse( Task stolenTask, WorkMessage msg) {
		
		Failure.Builder eb = Failure.newBuilder();
		eb.setId(state.getConf().getNodeId());
		eb.setRefId(msg.getHeader().getNodeId());
		eb.setMessage("No stealing needed");
		WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
		rb.setErr(eb);

		return rb.build();
		
	}
	
	private WorkMessage createTaskResponse( Task stolenTask) {
		
		

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(-1);
		hb.setTime(System.currentTimeMillis());

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setTask(stolenTask);
		wb.setSecret(1);
		return wb.build();
		
	}

	private Task stolenTask(){
		
		SimpleBalancer balancer = new SimpleBalancer();
		balancer.setTaskList(state.getTasks());
		
		return state.getTasks().rebalance();
		
	}

}