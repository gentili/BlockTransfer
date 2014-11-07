package ca.mcpnet.blocktransfer;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.thrift.server.AbstractNonblockingServer.FrameBuffer;
import org.apache.thrift.server.TNonblockingServer;

public class TBlockTransferServer extends TNonblockingServer {
	
	ConcurrentLinkedQueue<FrameBuffer> requestqueue;

	public TBlockTransferServer(AbstractNonblockingServerArgs args) {
		super(args);
		requestqueue = new ConcurrentLinkedQueue<FrameBuffer>();
	}

	@Override
	protected boolean requestInvoke(FrameBuffer frameBuffer) {
		requestqueue.offer(frameBuffer);
		return true;
	}
	
	/**
	 * This queue of requests is to be serviced only within the main
	 * minecraft server thread.  These requests may pull data out of
	 * or affect change inside the minecraft world.
	 */
	public void serviceRequestQueue() {
		for (FrameBuffer request = requestqueue.poll();request != null;request = requestqueue.poll()) {
			request.invoke();
		}
	}

}
