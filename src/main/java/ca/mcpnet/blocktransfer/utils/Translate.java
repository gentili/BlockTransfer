package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
	
	private Translate() {} // Prevent instantiation

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
								missingMappings.add(src_blkname);
								os.writeShort(src_blkid);
								os.writeByte(metadata);
								// System.out.println("No src_name->dst_name mapping for "+src_blkid+" -> "+src_blkname+" -> ?");
								continue;
							}
							Integer dst_blkid = dst_blknamemap.get(dst_blkname);
							if (dst_blkid == null)
								throw new RuntimeException("No dst_name->dst_id mapping for "+src_blkid+" -> "+src_blkname+" -> "+dst_blkname+" -> ?");
							blkidmap.put(src_blkid, dst_blkid);
							System.out.println("Mapping block "+src_blkname+" -> "+dst_blkname);
						}
						Integer dst_blkid = blkidmap.get(src_blkid);
						os.writeShort(dst_blkid);
						os.writeByte(metadata);
					}
			os.close();
		frame.setBlockdata(ba.toByteArray());
		return missingMappings;
	}

	public static Set<String> tiles(Map<Integer, String> src_itemidmap,
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
			// System.out.println("")
			if (id.contains("Chest")) {
				// System.out.println(nbt);
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
							missingMappings.add(src_itemname);
							System.out.println("No src_name->dst_name mapping for "+src_itemid+" -> "+src_itemname+" -> ?");
							continue;
						}
						Integer dst_itemid = dst_itemnamemap.get(dst_itemname);
						if (dst_itemid == null)
							throw new RuntimeException("No dst_name->dst_id mapping for "+src_itemid+" -> "+src_itemname+" -> "+dst_itemname+" -> ?");
						itemidmap.put((Integer) src_itemid, (Integer) dst_itemid);
						System.out.println("Mapping item "+src_itemname+" -> "+dst_itemname);
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
