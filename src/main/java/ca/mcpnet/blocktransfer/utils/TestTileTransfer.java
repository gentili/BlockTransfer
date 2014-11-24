package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTTileEntity;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTdVector;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public class TestTileTransfer {

	public static void main(String[] args) throws TException, IOException {
		String srcURL = "mindcrack.mcpnet.ca";
		String dstURL = "direwolf.mcpnet.ca";
		
		// Connect to src client
		/*
		System.out.println("Src client connect...");
		TFramedTransport src_transport = new TFramedTransport(new TSocket(srcURL,9090));
		src_transport.open();
		BlockTransferService.Client src_client = new BlockTransferService.Client(new TBinaryProtocol(src_transport));
		// Find source user
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
		iloc.x -= 4;
		iloc.y -= 4;
		iloc.z -= 4;
		BTiVector isize = new BTiVector(8,8,8);
		BTWorldFrame frame = src_client.getFrame(player.getWorldid(), iloc, isize);
		*/
		
		// Load src block id map
		Map<Integer, String> src_blkidmap = json.loadIdMap("Mindcrack.BlockIdMap.json"); 		
		// Load dst block id map
		Map<String, Integer> dst_blknamemap = json.loadNameMap("Direwolf.BlockNameMap.json"); 
		// Load block2block map
		HashMap<String, String> blkmap = json.loadBlockMap("Mindcrack.Direwolf.BlockMap.json");
		// Fetch the frame
		BTWorldFrame frame = binary.loadFrame("Mindcrack.frame.bin");
		// remap src blocks to dst blocks
		for (Iterator<BTTileEntity> btitr = frame.tilelist.iterator();btitr.hasNext();) {
			BTTileEntity bttile = btitr.next();
			NBTTagCompound bnt = CompressedStreamTools.read(new DataInputStream(new ByteArrayInputStream(bttile.getNbt())));
			Set keys = bnt.func_150296_c();
			System.out.println("---- "+bnt.getTag("id")+bnt);
			for (Iterator titr = keys.iterator();titr.hasNext();) {
				Object tilekey = titr.next();
				System.out.println(tilekey);
			}
		}
		
		/*
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
							if (dst_blkname == null)
								throw new RuntimeException("No src_name->dst_name mapping for "+src_blkid+" -> "+src_blkname+" -> ?");
							Integer dst_blkid = dst_blknamemap.get(dst_blkname);
							if (dst_blkid == null)
								throw new RuntimeException("No dst_name->dst_id mapping for "+src_blkid+" -> "+src_blkname+" -> "+dst_blkname+" -> ?");
							blkidmap.put(src_blkid, dst_blkid);
						}
						Integer dst_blkid = blkidmap.get(src_blkid);
						os.writeShort(dst_blkid);
						os.writeByte(metadata);
					}
			os.close();
		frame.setBlockdata(ba.toByteArray());
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
			System.out.println(player.getName());
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
		for (Iterator<BTTileEntity> itr = frame.tilelist.iterator();itr.hasNext();) {
			BTTileEntity tile = itr.next();
		}
		dst_client.putFrame(player.getWorldid(), iloc, frame);
		*/
	}
	

}
