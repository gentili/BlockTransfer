package ca.mcpnet.blocktransfer.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.thrift.TException;

import ca.mcpnet.blocktransfer.BlockTransferService;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class BuildBlockMap {

	public static void main(String[] args) throws IOException, TException {
		
		Map<Integer, String> dstlist = json.loadIdMap("Direwolf.BlockIdMap.json");
		Map<Integer, String> srclist = json.loadIdMap("Mindcrack.BlockIdMap.json");

		JsonObject blockmap = new JsonObject();
		for (int i = 0; i < 4096; i++) {
			String srcblock = srclist.get(i);
			String dstblock = dstlist.get(i);
			if (srcblock == null && dstblock == null)
				continue;
			if (srcblock == null)
				srcblock = Integer.toString(i);
			if (dstblock == null)
				dstblock = Integer.toString(i);
			blockmap.addProperty(srcblock, dstblock);
		}
		json.saveObject(blockmap,"Mindcrack.Direwolf.BlockMap.json");
	}
}
