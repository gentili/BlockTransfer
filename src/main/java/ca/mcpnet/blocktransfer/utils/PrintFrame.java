package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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

public class PrintFrame {

	public static void main(String[] args) throws TException, IOException {
		String prefix = "Direwolf";
		// Load src item id map
		Map<Integer, String> itemidmap = json.loadIntStringMap(prefix + ".ItemIdMap.json");
		Map<Integer, String> blockidmap = json.loadIntStringMap(prefix + ".BlockIdMap.json");

		// Fetch the frame
		BTWorldFrame frame = binary.loadFrame(prefix + ".frame.bin");

		// remap src blocks to dst blocks
		for (Iterator<BTTileEntity> bttileitr = frame.tilelist.iterator(); 
				bttileitr.hasNext();) {
			BTTileEntity bttile = bttileitr.next();
			NBTTagCompound nbt = CompressedStreamTools
					.read(new DataInputStream(new ByteArrayInputStream(bttile
							.getNbt())));
			String id = nbt.getString("id");
			if (id.contains("Chest")) {
				System.out.println(nbt);
				NBTTagList items = (NBTTagList) nbt.getTag("Items");
				for (int i = 0; i < items.tagCount(); i++) {
					NBTTagCompound itemstack = items.getCompoundTagAt(i);
					int src_itemid = itemstack.getShort("id");
					System.out.println("  "+itemidmap.get(src_itemid)+" "+itemstack);
				}
			} else {
					System.out.println(nbt);
			}
		}
	}
}
