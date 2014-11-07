package ca.mcpnet.blocktransfer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BlockTransferService.Client;

public class ThriftTestClient {

	public static void main(String[] args) {
		TTransport transport;
		
		try {
			transport = new TFramedTransport(new TSocket("localhost",9090));
			transport.open();
			
			TProtocol protocol = new TBinaryProtocol(transport);
			BlockTransferService.Client client = new BlockTransferService.Client(protocol);
			
			String resp = client.echo("Hey there!");
			System.out.println(resp);
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
