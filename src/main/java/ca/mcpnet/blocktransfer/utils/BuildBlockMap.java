package ca.mcpnet.blocktransfer.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class BuildBlockMap {

	public static void main(String[] args) throws IOException {
		JsonParser json = new JsonParser();
		HashMap<Integer, String> srclist = buildBlockIdMap(json
				.parse(new FileReader("BlockMap.Mindcrack.json")));
		HashMap<Integer, String> dstlist = buildBlockIdMap(json
				.parse(new FileReader("BlockMap.Direwolf.json")));

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
		FileWriter stringWriter = new FileWriter(
				"BlockMap.Mindcrack.Direwolf.json");
		JsonWriter jsonWriter = new JsonWriter(stringWriter);
		jsonWriter.setLenient(true);
		jsonWriter.setIndent("  ");
		Streams.write(blockmap, jsonWriter);
		jsonWriter.close();
	}

	public static HashMap<Integer, String> buildBlockIdMap(JsonElement root) {
		HashMap<Integer, String> blockidmap = new HashMap<Integer, String>();
		try {
			root = root.getAsJsonObject().get("0");
			root = root.getAsJsonObject().get("map");
			root = root.getAsJsonArray().get(3);
		} catch (NullPointerException e) {
			throw new RuntimeException("Missing expected members", e);
		}

		Set<Entry<String, JsonElement>> blocklist = root.getAsJsonObject()
				.entrySet();
		for (Iterator<Entry<String, JsonElement>> itr = blocklist.iterator(); itr
				.hasNext();) {
			Entry<String, JsonElement> block = itr.next();
			if (blockidmap.put(Integer.valueOf(block.getKey()), block
					.getValue().getAsString()) != null)
				throw new RuntimeException("Duplicate blockid!");
		}
		return blockidmap;
	}
}
