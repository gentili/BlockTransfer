package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTTileEntity;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTdVector;
import ca.mcpnet.blocktransfer.BTiVector;

public final class Translate {
	
	private Translate() { throw new RuntimeException("This is a static class"); } // Prevent instantiation

	/*
	 * Map all the blocks in a given Frame
	 */
	public static Set<String> blocks(Map<Integer, String> src_blkidmap,
			Map<String, Integer> dst_blknamemap,
			HashMap<String, String> blkmap, BTWorldFrame frame)
			throws IOException {
		// Missing src->dst map entries
		HashSet<String> missingMappings = new HashSet<String>();
		// Direct id to id map
		HashMap<Integer,Integer> blkidmap = new HashMap<Integer, Integer>();
		blkidmap.put(0, 0); // Add air block
		// Source blocks
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(frame.getBlockdata()));
		// Dst blocks
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(ba);
			for (int x = 0;x < frame.size.x;x++)
				for (int y = 0;y < frame.size.y;y++)
					for (int z = 0;z < frame.size.z;z++) {
						int src_blkid = is.readShort();
						int metadata = is.readByte();
						if (!blkidmap.containsKey(src_blkid)) {
							String src_blkname = src_blkidmap.get(src_blkid);
							if (src_blkname == null)
								throw new RuntimeException("No src_id->src_name mapping for "+src_blkid+" -> ?");
							String dst_blkname = blkmap.get(src_blkname);
							if (dst_blkname == null) {
								if (missingMappings.add(src_blkname)) 
									System.out.println("  !!!No src_name->dst_name mapping for "+src_blkid+" -> "+src_blkname+" -> ?");
								os.writeShort(57); // Replace with diamond
								os.writeByte(0);
								continue;
							}
							Integer dst_blkid = dst_blknamemap.get(dst_blkname);
							if (dst_blkid == null)
								throw new RuntimeException("No dst_name->dst_id mapping for "+src_blkid+" -> "+src_blkname+" -> "+dst_blkname+" -> ?");
							blkidmap.put(src_blkid, dst_blkid);
							System.out.println("  Mapping block "+src_blkname+" -> "+dst_blkname);
						}
						Integer dst_blkid = blkidmap.get(src_blkid);
						os.writeShort(dst_blkid);
						os.writeByte(metadata);
					}
			os.close();
		frame.setBlockdata(ba.toByteArray());
		return missingMappings;
	}

	/*
	 * Map all of the items in the tiles in a given frame 
	 */
	public static Set<String> itemsInTiles(Map<Integer, String> src_itemidmap,
			Map<String, Integer> dst_itemnamemap,
			HashMap<String, String> itemmap, BTWorldFrame frame)
			throws IOException {
		// Missing src->dst map entries
		HashSet<String> missingMappings = new HashSet<String>();
		// Direct id to id map
		HashMap<Integer,Integer> itemidmap = new HashMap<Integer, Integer>();
		// remap src blocks to dst blocks
		for (Iterator<BTTileEntity> bttileitr = frame.tilelist.iterator(); bttileitr
				.hasNext();) {
			BTTileEntity bttile = bttileitr.next();
			NBTTagCompound nbt = CompressedStreamTools
					.read(new DataInputStream(new ByteArrayInputStream(bttile
							.getNbt())));
			String id = nbt.getString("id");
			if (id.contains("Chest")) {
				NBTTagList items = (NBTTagList) nbt.getTag("Items");
				for (int i = 0; i < items.tagCount(); i++) {
					NBTTagCompound itemstack = items.getCompoundTagAt(i);
					int src_itemid = itemstack.getShort("id");
					if (!itemidmap.containsKey(src_itemid)) {
						String src_itemname = src_itemidmap.get(src_itemid);
						if (src_itemname == null)
							throw new RuntimeException("No src_id->src_name mapping for "+src_itemid+" -> ?");
						String dst_itemname = itemmap.get(src_itemname);
						if (dst_itemname == null) {
							if (missingMappings.add(src_itemname))
								System.out.println("  !!!No src_name->dst_name mapping for "+src_itemid+" -> "+src_itemname+" -> ?");
							continue;
						}
						Integer dst_itemid = dst_itemnamemap.get(dst_itemname);
						if (dst_itemid == null)
							throw new RuntimeException("No dst_name->dst_id mapping for "+src_itemid+" -> "+src_itemname+" -> "+dst_itemname+" -> ?");
						itemidmap.put((Integer) src_itemid, (Integer) dst_itemid);
						System.out.println("  Mapping item "+src_itemname+" -> "+dst_itemname);
					}
					int temp = itemidmap.get(src_itemid);
					if (temp > Short.MAX_VALUE)
						throw new RuntimeException("dst item id out of bounds!");
					short dst_itemid = (short) temp; 
					itemstack.setShort("id", dst_itemid);
				}
				// Now replace the binary array in the BTTileEntity with the modified NBT
				ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
				CompressedStreamTools.write(nbt, new DataOutputStream(bytearray));				
				bttile.setNbt(ByteBuffer.wrap(bytearray.toByteArray()));
			}
		}
		return missingMappings;
	}

	public static Set<String> tilesMindcrack147Direwolf1710(
			Map<Integer, String> src_itemidmap,
			Map<String, Integer> dst_itemnamemap,
			HashMap<String, String> itemmap, BTWorldFrame frame)
			throws IOException {
		HashSet<String> missingMappings = new HashSet<String>();
		List<BTTileEntity> tiles = frame.getTilelist();
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
	
				int dstid = Translate.mapItemId(nbt.getCompoundTag("item_type").getShort("id"), src_itemidmap,	itemmap, dst_itemnamemap);
	
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
				
				bttile.setNbt(binary.nbt2ByteArray(newnbt));
	
			} else if (id.contentEquals("net.minecraft.src.buildcraft.transport.GenericPipe")) {
				
				NBTTagCompound newnbt = new NBTTagCompound();
				newnbt.setString("id", "net.minecraft.src.buildcraft.transport.GenericPipe");
				newnbt.setByte("wireSet[0]", nbt.getByte("wireSet[0]"));
				newnbt.setByte("wireSet[1]", nbt.getByte("wireSet[1]"));
				newnbt.setByte("wireSet[2]", nbt.getByte("wireSet[2]"));
				newnbt.setByte("wireSet[3]", nbt.getByte("wireSet[3]"));
				int pipeid = Translate.mapItemId(nbt.getInteger("pipeId"), src_itemidmap,	itemmap, dst_itemnamemap);
				newnbt.setInteger("pipeId", pipeid);
				newnbt.setInteger("x",nbt.getInteger("x"));
				newnbt.setInteger("y",nbt.getInteger("y"));
				newnbt.setInteger("z",nbt.getInteger("z"));
				newnbt.setByte("redstoneInputSide[0]", (byte) 0);
				newnbt.setByte("redstoneInputSide[1]", (byte) 0);
				newnbt.setByte("redstoneInputSide[2]", (byte) 0);
				newnbt.setByte("redstoneInputSide[3]", (byte) 0);
				newnbt.setByte("redstoneInputSide[4]", (byte) 0);
				newnbt.setByte("redstoneInputSide[5]", (byte) 0);
				newnbt.setByte("outputOpen", (byte) 63);
				newnbt.setByte("outputOpen", (byte) 63);
	
				bttile.setNbt(binary.nbt2ByteArray(newnbt));
				/*
				// Direwolf
				Gate[1]:{wireState[0]:0b,wireState[1]:0b,wireState[2]:0b,direction:1,expansions:[],redstoneOutput:0b,material:"GOLD",logic:"AND",wireState[3]:0b,}e
				pluggable[1]:{ex:[],pluggableClass:"buildcraft.transport.gates.ItemGate$GatePluggable",mat:2b,logic:0b,}
	
				id:"net.minecraft.src.buildcraft.transport.GenericPipe"
				wireSet[0]:1b
				wireSet[1]:1b
				wireSet[2]:0b
				wireSet[3]:0b
				pipeId:4959
				x:-106
				y:88
				z:274
				travelingEntities:[]			
				redstoneInputSide[0]:0b
				redstoneInputSide[1]:0b
				redstoneInputSide[2]:0b
				redstoneInputSide[3]:0b
				redstoneInputSide[4]:0b
				redstoneInputSide[5]:0b
				outputOpen:63b
				inputOpen:63b
				
				// Mindcrack
				Gate:{Kind:4,}
	
				id:"net.minecraft.src.buildcraft.transport.GenericPipe"
				wireSet[0]:1b
				wireSet[1]:1b
				wireSet[2]:0b
				wireSet[3]:0b
				pipeId:4308
				x:-357
				y:138
				z:163
				travelingEntities:[]
	
				trigger[0]:0
				trigger[2]:0
				trigger[4]:0
				trigger[6]:0
				trigger[1]:0
				trigger[3]:0
				trigger[7]:0
				trigger[5]:0
				
				{facadeBlocks[2]:0,,facadeBlocks[0]:0,,facadeBlocks[4]:0,facadeMeta[4]:0,,,action[7]:0,,action[5]:0,,facadeMeta[1]:0,action[0]:0,,action[3]:0,facadeBlocks[3]:0,action[2]:0,facadeBlocks[1]:0,facadeMeta[5]:0,,,facadeBlocks[5]:0,,facadeMeta[3]:0,,,action[6]:0,,facadeMeta[2]:0,action[4]:0,,action[1]:0,,,facadeMeta[0]:0,
				
				*/
	
	
				// tilelist.add(bttile);
				// itr.remove();
			} else if (
					id.contentEquals("RCIronTankWallTile") ||
					id.contentEquals("RCIronTankGaugeTile") ||
					id.contentEquals("RCIronTankValveTile") ||
					id.contentEquals("ChickenChunkLoader") ||
					id.contains("Chest") 
					) {
				// Leave this tile alone
			} else {
				// Remove unrecognized tile
				if (missingMappings.add(id))
					System.out.println("  !!!No tile translation for "+id);
				itr.remove();
			}
			
		}
		return missingMappings;
	}

	public static Integer mapItemId(int src_itemid,
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
		// System.out.println("Mapping item "+src_itemname+" -> "+dst_itemname);
		return dst_itemid;
	}

	/////////////////////////////
	// Some handy vector utility functions
	/////////////////////////////
	
	public static BTiVector dloc2iloc(BTdVector dloc) {
		BTiVector iloc;
		iloc = new BTiVector((int)dloc.getX(),
				(int)dloc.getY(),
				(int)dloc.getZ());
		if (dloc.getX() < 0)
			iloc.x--;
		if (dloc.getZ() < 0)
			iloc.y--;
		return iloc;
	}
	
	public static BTiVector subtract(BTiVector lhs, BTiVector rhs) {
		return new BTiVector(lhs.x-rhs.x,lhs.y-rhs.y,lhs.z-rhs.z);
	}
	
	public static BTiVector add(BTiVector lhs, BTiVector rhs) {
		return new BTiVector(lhs.x+rhs.x,lhs.y+rhs.y,lhs.z+rhs.z);
	}

	////////////////////////
	// Random utility functions
	////////////////////////
	public static BTPlayer getPlayer(List<BTPlayer> src_plyrs, String name) {
		BTPlayer player = null;
		for (Iterator<BTPlayer> itr = src_plyrs.iterator();itr.hasNext();) {
			player = itr.next();
			if (player.getName().contentEquals(name)) {
				return player;
			}
		}
		throw new RuntimeException("Player not found!");
	}


}
