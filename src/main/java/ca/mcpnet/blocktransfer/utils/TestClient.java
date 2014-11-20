package ca.mcpnet.blocktransfer.utils;

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

import ca.mcpnet.blocktransfer.BTBlock;
import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;
import ca.mcpnet.blocktransfer.BlockTransferService.Client;

public class TestClient {

	public static void main(String[] args) {
		TTransport transport;
		
		try {
			transport = new TFramedTransport(new TSocket("localhost",9090));
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			BlockTransferService.Client client = new BlockTransferService.Client(protocol);
			
			System.out.println("Initial connect...");
			String ver = client.getVersion();
			System.out.println("ModVer: "+ ver);
			// Find the id for the diamond block
			int blockid = -1;
			Map<Integer, String> blockidmap = client.getBlockIdMap();
			for (Iterator<Entry<Integer, String>> bitr = blockidmap.entrySet().iterator();bitr.hasNext();) {
				Entry<Integer, String> block = bitr.next();
				// System.out.println(block.getKey() + "->" + block.getValue());
				if (block.getValue().contentEquals("tile.blockDiamond")) { // ("minecraft:diamond_block")) {
					blockid = block.getKey();
				}
			}
			if (blockid < 0) {
				throw new RuntimeException("Could not find diamond block");
			}
			System.out.println("Diamond block id: "+blockid);
			
			// Just pick the last player
			List<BTPlayer> playerlist = client.getPlayerList();
			BTPlayer player = null;
			for (Iterator<BTPlayer> pitr = playerlist.iterator();pitr.hasNext();) {
				player = pitr.next();
			}
			if (player == null) {
				throw new RuntimeException("Could not find a player");
			}
			System.out.println(player);
			
			// Put a diamond block above their head
			BTiVector iloc = new BTiVector((int)player.location.getX(),
					(int)player.location.getY(),
					(int)player.location.getZ());
			client.setBlock(player.getWorldid(), 
					iloc.setY(iloc.getY()+2), 
					new BTBlock(blockid, 0));
			
			// Load a far off block
			// BTBlock block = client.getBlock(player.getWorldid(), new BTiVector(10000,0,0));
			// System.out.println(block);
			
			// Grab a frame around the player
			iloc.x -= 4;
			iloc.y -= 4;
			iloc.z -= 4;
			BTiVector isize = new BTiVector(8,8,8);
			BTWorldFrame frame = client.getFrame(player.getWorldid(), iloc, isize);
			System.out.println(frame);
			System.out.println(frame.getTilelistSize());
			
			iloc.z += 8;
			client.putFrame(player.getWorldid(), iloc, frame);
			
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
