package ca.mcpnet.blocktransfer.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public final class FetchFrame {

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
				break;
			}
		}
		if (iloc == null)
			throw new RuntimeException("Could not find player!");
		// fetch frame around source user
		int radius = 2;
		int height = 1;
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		BTiVector isize = new BTiVector(radius*2,height*2+2,radius*2);
		BTWorldFrame frame = src_client.getFrame(player.getWorldid(), iloc, isize);
		binary.saveFrame(frame, "Direwolf.frame.bin");
	}

}
