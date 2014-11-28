package ca.mcpnet.blocktransfer.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import ca.mcpnet.blocktransfer.BlockTransferService.getBlockIdMap_result;
import ca.mcpnet.blocktransfer.BlockTransferService.getBlockNameMap_result;

public final class json {
	
	private json() { } // Prevent instantiation

	/**
	 * Open filename and read the json formatted blockidmap and return as Map 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 * @throws TTransportException
	 * @throws TException
	 */
	public static Map<Integer, String> loadIntStringMap(String filename)
			throws FileNotFoundException, TTransportException, TException {
		TIOStreamTransport JSONtransport = new TIOStreamTransport(
				new FileInputStream(filename));
		JSONtransport.open();
		TJSONProtocol JSONprotocol = new TJSONProtocol(JSONtransport);
		getBlockIdMap_result blockidmap_result = new getBlockIdMap_result();
		blockidmap_result.read(JSONprotocol);
		JSONtransport.close();
		return blockidmap_result.success;
	}

	/**
	 * Save the provided blockidmap to the given file in JSON format 
	 * @param blockidmap
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws TTransportException
	 * @throws TException
	 */
	public static void saveIdMap(Map<Integer, String> blockidmap, String filename)
			throws FileNotFoundException, TTransportException, TException {
		TIOStreamTransport JSONtransport = new TIOStreamTransport(
				new FileOutputStream(filename));
		JSONtransport.open();
		TJSONProtocol JSONprotocol = new TJSONProtocol(JSONtransport);
		getBlockIdMap_result blockidmap_result = new getBlockIdMap_result();
		blockidmap_result.success = blockidmap;
		blockidmap_result.write(JSONprotocol);
		JSONtransport.close();
	}
	
	/**
	 * Open filename and read the json formatted blocknamemap and return as Map
	 * @param filename
	 * @return
	 * @throws TException
	 * @throws FileNotFoundException
	 */
	public static Map<String, Integer> loadStringIntMap(String filename) throws TException, FileNotFoundException {
		TIOStreamTransport JSONtransport = new TIOStreamTransport(
				new FileInputStream(filename));
		JSONtransport.open();
		TJSONProtocol JSONprotocol = new TJSONProtocol(JSONtransport);
		getBlockNameMap_result blocknamemap_result = new getBlockNameMap_result();
		blocknamemap_result.read(JSONprotocol);
		JSONtransport.close();
		return blocknamemap_result.success;
	}

	/**
	 * Save the provided blocknamemap to the given file in JSON format 
	 * @param blocknamemap
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws TException
	 */
	public static void saveNameMap(Map<String,Integer> blocknamemap,String filename)
			throws FileNotFoundException, TException {
		TIOStreamTransport transport = new TIOStreamTransport(
				new FileOutputStream(filename));
		transport.open();
		TJSONProtocol protocol = new TJSONProtocol(transport);
		getBlockNameMap_result blockidmap_result = new getBlockNameMap_result();
		blockidmap_result.success = blocknamemap;
		blockidmap_result.write(protocol);
		transport.close();
	}

	/**
	 * Load the blockmap from the given filename and return as Map
	 * @param filename
	 * @return
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 * @throws FileNotFoundException
	 */

	public static HashMap<String, String> loadStringStringMap(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonParser json = new JsonParser();
		JsonElement root = json.parse(new FileReader(filename));
		
		HashMap<String, String> blockmap = new HashMap<String, String>();	
		Set<Entry<String, JsonElement>> blocklist = root.getAsJsonObject()
				.entrySet();
		for (Iterator<Entry<String, JsonElement>> itr = blocklist.iterator(); itr
				.hasNext();) {
			Entry<String, JsonElement> block = itr.next();
			if (blockmap.put(block.getKey(), block.getValue().getAsString()) != null)
				throw new RuntimeException("Duplicate blockname!");
		}
		return blockmap;
	}

	/**
	 * 
	 * @param Write the given JSON object to the given file
	 * @param filename
	 * @throws IOException
	 */
	static void saveObject(JsonObject jsonObject,String filename) throws IOException {
		FileWriter stringWriter = new FileWriter(
				filename);
		JsonWriter jsonWriter = new JsonWriter(stringWriter);
		jsonWriter.setLenient(true);
		jsonWriter.setIndent("  ");
		Streams.write(jsonObject, jsonWriter);
		jsonWriter.close();
	}





}
