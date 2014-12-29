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
		System.out.println("Dst client connect...");
		TFramedTransport dst_transport = new TFramedTransport(new TSocket("10.10.10.5",9090));
		dst_transport.open();
		BlockTransferService.Client dst_client = new BlockTransferService.Client(new TBinaryProtocol(dst_transport));
		// Find dst user
		BTPlayer dst_player = Translate.getPlayer(dst_client.getPlayerList(),"globnobulous");
		// Load src block id map
		Map<Integer, String> src_blkidmap = dst_client.getBlockIdMap();
		
		BTiVector iloc = Translate.dloc2iloc(dst_player.location);
		// fetch frame around source user
		int radius = 0;
		int height = 1;
		iloc.x -= radius;
		iloc.y -= height;
		iloc.z -= radius;
		BTiVector isize = new BTiVector(radius*2+1,height*2+2,radius*2+1);
		BTWorldFrame frame = dst_client.getFrame(dst_player.getWorldid(), iloc, isize);

		DataInputStream is = new DataInputStream(new ByteArrayInputStream(frame.getBlockdata()));
		// Dst blocks
		for (int x = 0;x < frame.size.x;x++)
			for (int y = 0;y < frame.size.y;y++) 
				for (int z = 0;z < frame.size.z;z++)
				{
					int src_blkid = is.readShort();
					int metadata = is.readByte();
					if (src_blkid == 0)
						continue;
					System.out.println(x+","+y+","+z+" "+src_blkidmap.get(src_blkid));
				}
	}
}
