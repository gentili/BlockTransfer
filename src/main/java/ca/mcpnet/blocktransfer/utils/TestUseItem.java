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

public class TestUseItem {

	public static void main(String[] args) throws TException {
		TTransport transport;

		transport = new TFramedTransport(new TSocket("10.10.10.5", 9090));
		transport.open();
		TProtocol protocol = new TBinaryProtocol(transport);
		BlockTransferService.Client client = new BlockTransferService.Client(
				protocol);

		System.out.println("Initial connect...");
		String ver = client.getVersion();
		System.out.println("ModVer: " + ver);
		// Find the id for the diamond block
		Integer itemid = -1;
		Map<String, Integer> itemnamemap = client.getItemNameMap();
		for (Iterator<String> itr = itemnamemap.keySet().iterator(); itr.hasNext();) {
			String itemname = itr.next();
			if (itemname.contains("pipe"))
				System.out.println(itemname);
			
		}
		itemid = itemnamemap.get("BuildCraft|Transport:item.buildcraftPipe.pipeitemsstone");
		if (itemid == null) {
			throw new RuntimeException("Could not find block");
		}

		// Just pick the first player
		List<BTPlayer> playerlist = client.getPlayerList();
		BTPlayer player = playerlist.iterator().next();
		if (player == null) {
			throw new RuntimeException("Could not find a player");
		}
		System.out.println(player);

		// Use the item on an air block above their head
		BTiVector iloc = new BTiVector((int) player.location.getX(),
				(int) player.location.getY(), (int) player.location.getZ());
		iloc.x += 5;
		System.out.println(iloc);
		client.setBlock(player.worldid, iloc, new BTBlock(0,0));
		client.useItem(player.worldid, iloc, player.name, (byte) 4, itemid);
	}
	
	
	

}
