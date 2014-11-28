package ca.mcpnet.blocktransfer.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class TestBlockTranslate {

	public static void main(String[] args) throws TException, IOException {
		
		// Load src block id map
		Map<Integer, String> src_blkidmap = json.loadIntStringMap("Mindcrack.BlockIdMap.json"); 		
		// Load dst block id map
		Map<String, Integer> dst_blknamemap = json.loadStringIntMap("Direwolf.BlockNameMap.json"); 
		// Load block2block map
		HashMap<String, String> blkmap = json.loadStringStringMap("Mindcrack.Direwolf.BlockMap.json");
		// Fetch the frame
		BTWorldFrame frame = binary.loadFrame("Mindcrack.frame.bin");
		// remap src blocks to dst blocks
		
		Set<String> missing = Translate.blocks(src_blkidmap, dst_blknamemap, blkmap, frame);
		for (Iterator<String> itr = missing.iterator(); itr.hasNext(); ) 
			System.out.println('"'+itr.next()+"\": \"\",");
	}
}
