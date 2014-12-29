package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

public class Mindcrack2DirewolfCopy {

	public static class FrameTransfer {
		
		public FrameTransfer(String nm, BTiVector src, BTiVector dst, BTiVector size) {
			name = nm;
			srcvec = src;
			dstvec = dst;
			sizevec = size;
		}
		
		public String name;
		public BTiVector srcvec;
		public BTiVector dstvec;
		public BTiVector sizevec;
		
	}
	
	public static void main(String[] args) throws TException, IOException {
		String srcURL = "mindcrack.mcpnet.ca";
		String dstURL = "10.10.10.5"; // "direwolf.mcpnet.ca";
		int src_worldid = 0;
		int dst_worldid = 0;
		
		////////////////////////
		// Connect to src client
		////////////////////////
		System.out.println("Src client connect...");
		TFramedTransport src_transport = new TFramedTransport(new TSocket(srcURL,9090));
		src_transport.open();
		BlockTransferService.Client src_client = new BlockTransferService.Client(new TBinaryProtocol(src_transport));
		// Find source user
		// BTPlayer src_player = Translate.getPlayer(src_client.getPlayerList(),"globnobulous");
		// System.out.println(src_player);
		
		////////////////////////
		// Connect to dst client
		////////////////////////
		System.out.println("Dst client connect...");
		TFramedTransport dst_transport = new TFramedTransport(new TSocket(dstURL,9090));
		dst_transport.open();
		BlockTransferService.Client dst_client = new BlockTransferService.Client(new TBinaryProtocol(dst_transport));
		// Find dst user
		BTPlayer dst_player = Translate.getPlayer(dst_client.getPlayerList(),"globnobulous");

		System.out.println("Loading lists...");

		// Load src block id map
		Map<Integer, String> src_blkidmap = src_client.getBlockIdMap(); 		
		// Load dst block id map
		Map<String, Integer> dst_blknamemap = dst_client.getBlockNameMap(); 
		// Load block2block map
		HashMap<String, String> blkmap = json.loadStringStringMap("Mindcrack.Direwolf.VanillaBlockMap.json");
		
		// Load src item id map
		Map<Integer, String> src_itemidmap = src_client.getItemIdMap();
		// Load dst item id map
		Map<String, Integer> dst_itemnamemap = dst_client.getItemNameMap();
		// Load block2block map
		HashMap<String, String> itemmap = json.loadStringStringMap("Mindcrack.Direwolf.ItemMap.json");
		

		////////////////////////
		// Set up frames for transfer
		////////////////////////

		Vector<FrameTransfer> frametransferlist = new Vector<FrameTransfer>();
		
		BTiVector dstorigin = Translate.dloc2iloc(dst_player.location);
		// frametransferlist.add(new FrameTransfer("Tower",new BTiVector(-267,61,299), Translate.add(dstorigin, new BTiVector(20,0,0)), new BTiVector(23,67,23)));
		frametransferlist.add(new FrameTransfer("Mountain",new BTiVector(-368,95,106), Translate.add(dstorigin, new BTiVector(5,0,-40)).setY(10), new BTiVector(52,40,74)));
		
		////////////////////////
		// Do the frame transfers
		////////////////////////

		for (FrameTransfer itr : frametransferlist) {
			
			System.out.println("Fetching frame "+itr.name);
			BTWorldFrame frame = src_client.getFrame(src_worldid, itr.srcvec, itr.sizevec);
			
			System.out.println("Doing tile translations...");
			Set<String> missingtiles = Translate.tilesMindcrack147Direwolf1710(src_itemidmap, dst_itemnamemap, itemmap, frame);
			
			System.out.println("Doing block translations...");
			Set<String> missingblocks = Translate.blocks(src_blkidmap, dst_blknamemap, blkmap, frame);
			
			System.out.println("Doing item translations...");
			Set<String> missingitems = Translate.itemsInTiles(src_itemidmap, dst_itemnamemap, itemmap, frame);
			
			System.out.println("Depositing frame "+itr.name);
			dst_client.putFrame(dst_worldid, itr.dstvec, frame);
			
			for (String block : missingblocks) {
				System.out.println(">>> Missing "+block);
			}
			/*
			BTiVector corner = itr.getLeft().deepCopy();
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //0
			corner.x += itr.getRight().x;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //1
			corner.y += itr.getRight().y;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //2
			corner.z += itr.getRight().z;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //3
			corner.x = itr.getLeft().x;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //4
			corner.y = itr.getLeft().y;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //5
			corner.x += itr.getRight().x;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //7
			corner.x = itr.getLeft().x;
			corner.y += itr.getRight().y;
			corner.z = itr.getLeft().z;
			dst_client.setBlock(dst_worldid, Translate.add(dst_origin,corner), new BTBlock(89,0)); //8
			*/
			System.out.println("Done");
		}
	}

}
