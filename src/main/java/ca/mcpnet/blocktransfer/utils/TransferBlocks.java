package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BTBlock;
import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTTileEntity;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTdVector;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public class TransferBlocks {

	public static void main(String[] args) throws TException, IOException {
		String srcURL = "mindcrack.mcpnet.ca";
		String dstURL = "10.10.10.5"; // "direwolf.mcpnet.ca";
		BTiVector iloc = null;
		BTPlayer player = null;
		
		// Connect to src client
		System.out.println("Src client connect...");
		TFramedTransport src_transport = new TFramedTransport(new TSocket(srcURL,9090));
		src_transport.open();
		BlockTransferService.Client src_client = new BlockTransferService.Client(new TBinaryProtocol(src_transport));
		// Find source user
		player = Translate.getPlayer(src_client.getPlayerList(),"globnobulous");
		iloc = Translate.dloc2iloc(player.location);
		// fetch frame around source user
		/*
		int radius = 10;
		int height = 2;
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		BTiVector isize = new BTiVector(radius*2,height*2+2,radius*2);
		*/
		iloc = new BTiVector();
		iloc.x = -408;
		iloc.z = 106;
		iloc.y = 66;
		BTiVector isize = new BTiVector();
		isize.x = 80;
		isize.y = 60;
		isize.z = 80;
		
		
		BTWorldFrame frame = src_client.getFrame(player.getWorldid(), iloc, isize);
		
		System.out.println("Loading lists and frames and such...");
		// BTWorldFrame frame = binary.loadFrame("Mindcrack.frame.bin");

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
		Set<String> missingblocks = Translate.blocks(src_blkidmap, dst_blknamemap, blkmap, frame);
		// remap src items to dst items
		Set<String> missingitems = Translate.itemsInTiles(src_itemidmap, dst_itemnamemap, itemmap, frame);
		// Connect to dst client
		if (!missingblocks.isEmpty() || !missingitems.isEmpty()) {
			System.out.println("Missing blocks");			
			for (Iterator<String> itr = missingblocks.iterator(); itr.hasNext(); ) 
				System.out.println('"'+itr.next()+"\": \"\",");
			
			System.out.println("Missing items");
			for (Iterator<String> itr = missingitems.iterator(); itr.hasNext(); ) 
				System.out.println('"'+itr.next()+"\": \"\",");

			throw new RuntimeException("There are blocks or items with no translation in this frame");
		}
		
		System.out.println("Dst client connect...");
		TFramedTransport dst_transport = new TFramedTransport(new TSocket(dstURL,9090));
		dst_transport.open();
		BlockTransferService.Client dst_client = new BlockTransferService.Client(new TBinaryProtocol(dst_transport));
		// Find dst user
		player = Translate.getPlayer(dst_client.getPlayerList(),"globnobulous");
		iloc = Translate.dloc2iloc(player.location);
		// fetch frame around source user
		/*
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		*/
		System.out.println("Depositing frame...");
		// dst_client.putFrame(player.getWorldid(), iloc, frame);
		System.out.println("Done");
	}
}
