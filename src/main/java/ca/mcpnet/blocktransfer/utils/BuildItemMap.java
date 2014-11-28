package ca.mcpnet.blocktransfer.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.thrift.TException;

import ca.mcpnet.blocktransfer.BlockTransferService;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class BuildItemMap {

	public static void main(String[] args) throws IOException, TException {
		
		System.out.print("Loading src map...");
		Map<Integer, String> srclist = json.loadIntStringMap("Mindcrack.ItemIdMap.json");
		System.out.print("Loading dst map...");
		Map<Integer, String> dstlist = json.loadIntStringMap("Direwolf.ItemIdMap.json");
		System.out.print("Building src->dst map...");
		JsonObject itemmap = new JsonObject();
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			String srcblock = srclist.get(i);
			String dstblock = dstlist.get(i);
			if (srcblock == null && dstblock == null)
				continue;
			if (srcblock == null)
				srcblock = Integer.toString(i);
			if (dstblock == null)
				dstblock = Integer.toString(i);
			itemmap.addProperty(srcblock, dstblock);
		}
		System.out.print("Saving map...");
		json.saveObject(itemmap,"Mindcrack.Direwolf.ItemMap.json");
		System.out.println("Done");
	}
}
