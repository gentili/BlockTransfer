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
		player = Translate.getPlayer(src_client.getPlayerList(),"globnobulous");
		iloc = Translate.dloc2iloc(player.location);
		// fetch frame around source user
		int radius = 3;
		int height = 1;
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		BTiVector isize = new BTiVector(radius*2+1,height*2+2,radius*2+1);
		BTWorldFrame frame = src_client.getFrame(player.getWorldid(), iloc, isize);
		binary.saveFrame(frame, "Direwolf.frame.bin");
	}

}
