package ca.mcpnet.blocktransfer.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTTileEntity;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTdVector;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public class TransferBlocks {

	public static void main(String[] args) throws TException, IOException {
		String srcURL = "mindcrack.mcpnet.ca";
		String dstURL = "localhost"; // "direwolf.mcpnet.ca";
		BTiVector iloc = null;
		BTPlayer player = null;
		
		/*
		// Connect to src client
		System.out.println("Src client connect...");
		TFramedTransport src_transport = new TFramedTransport(new TSocket(srcURL,9090));
		src_transport.open();
		BlockTransferService.Client src_client = new BlockTransferService.Client(new TBinaryProtocol(src_transport));
		// Find source user
		List<BTPlayer> src_plyrs = src_client.getPlayerList();
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
		iloc.x -= 4;
		iloc.y -= 4;
		iloc.z -= 4;
		BTiVector isize = new BTiVector(8,8,8);
		BTWorldFrame frame = src_client.getFrame(player.getWorldid(), iloc, isize);
		
		*/
		System.out.println("Loading lists and frames and such...");
		BTWorldFrame frame = binary.loadFrame("Mindcrack.frame.bin");

		// Load src block id map
		Map<Integer, String> src_blkidmap = json.loadIntStringMap("Mindcrack.BlockIdMap.json"); 		
		// Load dst block id map
		Map<String, Integer> dst_blknamemap = json.loadStringIntMap("Direwolf.BlockNameMap.json"); 
		// Load block2block map
		HashMap<String, String> blkmap = json.loadStringStringMap("Mindcrack.Direwolf.BlockMap.json");
		
		// Load src item id map
		Map<Integer, String> src_itemidmap = json.loadIntStringMap("Mindcrack.ItemIdMap.json");
		// Load dst item id map
		Map<String, Integer> dst_itemnamemap = json.loadStringIntMap("Direwolf.ItemNameMap.json");
		// Load block2block map
		HashMap<String, String> itemmap = json.loadStringStringMap("Mindcrack.Direwolf.ItemMap.json");

		System.out.println("Doing translations...");
		// remap src blocks to dst blocks
		Translate.blocks(src_blkidmap, dst_blknamemap, blkmap, frame);
		// remap src tiles to dst tiles
		Translate.tiles(src_itemidmap, dst_itemnamemap, itemmap, frame);
		// Connect to dst client
		
		System.out.println("Dst client connect...");
		TFramedTransport dst_transport = new TFramedTransport(new TSocket(dstURL,9090));
		dst_transport.open();
		BlockTransferService.Client dst_client = new BlockTransferService.Client(new TBinaryProtocol(dst_transport));
		// Find dst user
		List<BTPlayer> dst_plyrs = dst_client.getPlayerList();
		iloc = null;
		player = null;
		for (Iterator<BTPlayer> itr = dst_plyrs.iterator();itr.hasNext();) {
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
		iloc.x -= 4;
		iloc.y -= 4;
		iloc.z -= 4;
		System.out.println("Depositing frame...");
		dst_client.putFrame(player.getWorldid(), iloc, frame);
	}
	

}
