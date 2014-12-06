package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
		int radius = 2;
		int height = 2;
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		BTiVector isize = new BTiVector(radius*2,height*2+2,radius*2);
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

		System.out.println("Removing dead entities...");
		List<BTTileEntity> tiles = frame.getTilelist();
		List<BTTileEntity> tilelist = new ArrayList<BTTileEntity>();
		for (Iterator<BTTileEntity> itr = tiles.iterator(); itr.hasNext();) {
			BTTileEntity bttile = itr.next();
			NBTTagCompound nbt = CompressedStreamTools
					.read(new DataInputStream(new ByteArrayInputStream(bttile
							.getNbt())));
			String id = nbt.getString("id");
			if (id.contains("GenericPipe") || id.contains("factory_barrel")) {
				tilelist.add(bttile);
				itr.remove();
			}
			
		}
		System.out.println("Doing translations...");
		// remap src blocks to dst blocks
		Set<String> missingblocks = Translate.blocks(src_blkidmap, dst_blknamemap, blkmap, frame);
		// remap src tiles to dst tiles
		Set<String> missingitems = Translate.tiles(src_itemidmap, dst_itemnamemap, itemmap, frame);
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
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		System.out.println("Depositing frame...");
		// dst_client.putFrame(player.getWorldid(), iloc, frame);
		System.out.println("Filling in specials...");
		for (Iterator<BTTileEntity> itr = tilelist.iterator(); itr.hasNext(); ) {
			BTTileEntity bttile = itr.next();
			NBTTagCompound nbt = CompressedStreamTools
					.read(new DataInputStream(new ByteArrayInputStream(bttile
							.getNbt())));

			String id = nbt.getString("id");
			if (id.contentEquals("factory_barrel")) {
				/*				
				id:"factory_barrel2",
				count:193,
				dir:8,
				item:{id:4s,Damage:0s,Count:0b,},
				slab:{id:574s,Damage:7s,Count:6b,},
				facing:3b,
				draw_active_byte:0b,
				ver:"1.7.10-0.8.88.1",
				type:0,
				x:-116
				y:81
				z:240
				log:{id:553s,Damage:3s,Count:1b,}
				
				id:"factory_barrel",
				item_count:1570,
				item_type:{id:1315s,Damage:0s,Count:64b,}

				upgrade:0,
				facing:5b,
				draw_active_byte:0b,
				ver:"0.7.21",
				x:-351,
				y:113,
				z:163,
				
				*/
				
				//dst_client.setBlock(player.worldid, add(bttile.location,iloc), new BTBlock(barrelid,0));
				//dst_client.useItem(player.worldid,add(bttile.location,iloc),player.name, (byte) 3, barrelid);
				int src_itemid = nbt.getCompoundTag("item_type").getShort("id");
				//Integer dst_itemid = mapitemid(src_itemid, src_itemidmap,
					//	itemmap, dst_itemnamemap);
				//  dst_client.useItem(player.worldid,add(bttile.location,iloc),player.name, (byte) 3, dst_itemid);
				
			} else if (id.contentEquals("net.minecraft.src.buildcraft.transport.GenericPipe")) {
				int src_itemid = nbt.getInteger("pipeId");
				int dst_itemid = mapitemid(src_itemid, src_itemidmap,
						itemmap, dst_itemnamemap);
				dst_client.useItem(player.worldid,add(bttile.location,iloc),player.name, (byte) 3, dst_itemid);
				if (dst_itemid == 4986)
					System.out.println(nbt);
			}
		}
		System.out.println("Done");
	}

	public static Integer mapitemid(int src_itemid,
			Map<Integer, String> src_itemidmap,
			HashMap<String, String> srcdst_itemmap,
			Map<String, Integer> dst_itemnamemap) {
		String src_itemname = src_itemidmap.get(src_itemid);
		if (src_itemname == null)
			throw new RuntimeException("No src_id->src_name mapping for "+src_itemid+" -> ?");
		String dst_itemname = srcdst_itemmap.get(src_itemname);
		if (dst_itemname == null) {
			throw new RuntimeException("No src_name->dst_name mapping for "+src_itemname+" -> ?");
		}
		Integer dst_itemid = dst_itemnamemap.get(dst_itemname);
		if (dst_itemid == null)
			throw new RuntimeException("No dst_name->dst_id mapping for "+src_itemid+" -> "+src_itemname+" -> "+dst_itemname+" -> ?");
		return dst_itemid;
	}
	
	public static BTiVector add(BTiVector lhs, BTiVector rhs) {
		return new BTiVector(lhs.x+rhs.x,lhs.y+rhs.y,lhs.z+rhs.z);
	}

}
