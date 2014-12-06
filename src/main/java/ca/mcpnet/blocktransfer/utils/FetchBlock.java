package ca.mcpnet.blocktransfer.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import ca.mcpnet.blocktransfer.BTBlock;
import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public final class FetchBlock {

	public static void main(String[] args) throws TException, IOException {
		TFramedTransport src_transport = new TFramedTransport(new TSocket("10.10.10.5",9090));
		src_transport.open();
		BlockTransferService.Client src_client = new BlockTransferService.Client(new TBinaryProtocol(src_transport));
		List<BTPlayer> src_plyrs = src_client.getPlayerList();
		BTiVector iloc = null;
		BTPlayer player = null;
		for (Iterator<BTPlayer> itr = src_plyrs.iterator();itr.hasNext();) {
			player = itr.next();
			if (player.getName().contentEquals("globnobulous")) {
				iloc = new BTiVector((int)player.location.getX(),
						(int)player.location.getY(),
						(int)player.location.getZ());
				if (player.location.getX() < 0)
					iloc.x++;
				if (player.location.getZ() < 0)
					iloc.y++;
				break;
			}
		}
		if (iloc == null)
			throw new RuntimeException("Could not find player!");
		// fetch block below user
		iloc.y--;
		BTBlock block = src_client.getBlock(player.getWorldid(), iloc);
		System.out.println(block);
	}

}
