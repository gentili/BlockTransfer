package ca.mcpnet.blocktransfer;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class ThriftTestServer {

	public static BlockTransferServiceHandler handler;
	public static BlockTransferService.Processor processor;

	public static void main(String[] args) {

		handler = new BlockTransferServiceHandler();
		processor = new BlockTransferService.Processor(handler);

		try {
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(
					9090);
			TNonblockingServer.Args serverArgs = new TNonblockingServer.Args(serverTransport);
			serverArgs.processor(processor);
			serverArgs.transportFactory(new TFramedTransport.Factory());
			serverArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
			final TBlockTransferServer server = new TBlockTransferServer(serverArgs);
			System.out.println("Starting test thrift server...");
			Thread t = new Thread() {
				public void run() {
					server.serve();
				}
			};
			t.start();
			while (true) {
				Thread.sleep(1000);
				server.serviceRequestQueue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
