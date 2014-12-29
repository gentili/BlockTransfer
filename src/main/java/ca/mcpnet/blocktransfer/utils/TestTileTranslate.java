package ca.mcpnet.blocktransfer.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTdVector;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public class TestTileTranslate {

	public static void main(String[] args) throws TException, IOException {
		// Load src item id map
		Map<Integer, String> src_itemidmap = json
				.loadIntStringMap("Mindcrack.ItemIdMap.json");
		// Load dst item id map
		Map<String, Integer> dst_itemnamemap = json
				.loadStringIntMap("Direwolf.ItemNameMap.json");
		// Load block2block map
		HashMap<String, String> itemmap = json
				.loadStringStringMap("Mindcrack.Direwolf.ItemMap.json");

		// Fetch the frame
		BTWorldFrame frame = binary.loadFrame("Mindcrack.frame.bin");

		Set<String> missing = Translate.itemsInTiles(src_itemidmap, dst_itemnamemap, itemmap, frame);
		for (Iterator<String> itr = missing.iterator(); itr.hasNext(); ) 
			System.out.println('"'+itr.next()+"\": \"\",");

	}
}
