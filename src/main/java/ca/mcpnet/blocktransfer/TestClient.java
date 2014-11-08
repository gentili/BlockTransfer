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
			System.out.println("ModVer: "+ ver);
			// Find the id for the diamond block
			int blockid = -1;
			Map<Integer, String> blockidmap = client.getBlockIdMap();
			for (Iterator<Entry<Integer, String>> bitr = blockidmap.entrySet().iterator();bitr.hasNext();) {
				Entry<Integer, String> block = bitr.next();
				// System.out.println(block.getKey() + "->" + block.getValue());
				if (block.getValue().contentEquals("minecraft:diamond_block")) {
					blockid = block.getKey();
				}
			}
			if (blockid < 0) {
				throw new RuntimeException("Could not find diamond block");
			}
			System.out.println("Diamond block id: "+blockid);
			// Just pick the last player for now
			List<BTPlayer> playerlist = client.getPlayerList();
			BTPlayer player = null;
			for (Iterator<BTPlayer> pitr = playerlist.iterator();pitr.hasNext();) {
				player = pitr.next();
			}
			if (player == null) {
				throw new RuntimeException("Could not find a player");
			}
			System.out.println(player);
			BTiVector iloc = new BTiVector((int)player.location.getX(),
					(int)player.location.getY(),
					(int)player.location.getZ());
			client.setBlock(player.getWorldid(), 
					iloc, 
					new BTBlock(blockid, 15));
			client.setBlock(player.getWorldid(), 
					iloc.setY(iloc.getY()+1), 
					new BTBlock(blockid, 17));
			BTBlock block = client.getBlock(player.getWorldid(), new BTiVector(10000,0,0));
			System.out.println(block);
			
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
