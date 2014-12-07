package ca.mcpnet.blocktransfer.utils;

import java.io.IOException;

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
		BTPlayer player = Translate.getPlayer(src_client.getPlayerList(),"globnobulous");
		BTiVector iloc = Translate.dloc2iloc(player.location);
		// fetch block below user
		iloc.y--;
		BTBlock block = src_client.getBlock(player.getWorldid(), iloc);
		System.out.println(block);
	}

}
