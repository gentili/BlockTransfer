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
		int radius = 10;
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

		System.out.println("Processing special tiles...");
		List<BTTileEntity> tiles = frame.getTilelist();
		List<BTTileEntity> tilelist = new ArrayList<BTTileEntity>();
		for (Iterator<BTTileEntity> itr = tiles.iterator(); itr.hasNext();) {
			BTTileEntity bttile = itr.next();
			NBTTagCompound nbt = CompressedStreamTools
					.read(new DataInputStream(new ByteArrayInputStream(bttile
							.getNbt())));
			String id = nbt.getString("id");
			if (id.contentEquals("factory_barrel")) {
				NBTTagCompound slab = new NBTTagCompound();
				slab.setShort("id", (short) 574);
				slab.setShort("Damage", (short) 7);
				slab.setByte("Count", (byte) 6);
				
				NBTTagCompound log = new NBTTagCompound();
				log.setShort("id", (short) 553);
				log.setShort("Damage", (short) 3);
				log.setByte("Count", (byte) 1);

				int dstid = mapitemid(nbt.getCompoundTag("item_type").getShort("id"), src_itemidmap,	itemmap, dst_itemnamemap);

				NBTTagCompound item = new NBTTagCompound();
				item.setShort("id", (short) dstid);
				item.setShort("Damage", (short) 0);
				item.setByte("Count",(byte) 0);
				
				// Create new nbt
				NBTTagCompound newnbt = new NBTTagCompound();
				newnbt.setString("id", "factory_barrel2");
				newnbt.setInteger("count", nbt.getInteger("item_count"));
				newnbt.setInteger("dir", nbt.getByte("facing")*4);
				newnbt.setByte("draw_active_byte", nbt.getByte("draw_active_byte"));
				newnbt.setInteger("x",nbt.getInteger("x"));
				newnbt.setInteger("y",nbt.getInteger("y"));
				newnbt.setInteger("z",nbt.getInteger("z"));
				newnbt.setTag("item", item);
				
				newnbt.setString("ver", "1.7.10-0.8.88.1");
				newnbt.setInteger("type", 0);
				newnbt.setTag("slab", slab);
				newnbt.setTag("log", log);
				newnbt.setByte("facing", (byte) 3);
				
				ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
					CompressedStreamTools.write(newnbt, new DataOutputStream(bytearrayoutputstream));
				ByteBuffer bytearray = ByteBuffer.wrap(bytearrayoutputstream.toByteArray());
				
				bttile.setNbt(bytearray);

			} else if (id.contains("GenericPipe")) {
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
		player = Translate.getPlayer(dst_client.getPlayerList(),"globnobulous");
		iloc = Translate.dloc2iloc(player.location);
		// fetch frame around source user
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		System.out.println("Depositing frame...");
		dst_client.putFrame(player.getWorldid(), iloc, frame);
		System.out.println("Filling in specials...");
		for (Iterator<BTTileEntity> itr = tilelist.iterator(); itr.hasNext(); ) {
			BTTileEntity bttile = itr.next();
			NBTTagCompound nbt = CompressedStreamTools
					.read(new DataInputStream(new ByteArrayInputStream(bttile
							.getNbt())));

			String id = nbt.getString("id");
			if (id.contentEquals("net.minecraft.src.buildcraft.transport.GenericPipe")) {
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
		if (src_itemid == 0)
			return 0;
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
