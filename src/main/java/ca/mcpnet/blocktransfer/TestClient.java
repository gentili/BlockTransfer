package ca.mcpnet.blocktransfer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BlockTransferService.Client;

public class TestClient {

	public static void main(String[] args) {
		TTransport transport;
		
		try {
			transport = new TFramedTransport(new TSocket("localhost",9090));
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			BlockTransferService.Client client = new BlockTransferService.Client(protocol);
			
			String ver = client.getVersion();
			System.out.println(ver);
			Map<Integer, String> blockidmap = client.getBlockIdMap();
			for (Iterator<Entry<Integer, String>> bitr = blockidmap.entrySet().iterator();bitr.hasNext();) {
				Entry<Integer, String> block = bitr.next();
				System.out.println(block.getKey() + "->" + block.getValue());
			}
			List<BTPlayer> playerlist = client.getPlayerList();
			for (Iterator<BTPlayer> pitr = playerlist.iterator();pitr.hasNext();) {
				BTPlayer player = pitr.next();
				System.out.println(player);
			}
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
