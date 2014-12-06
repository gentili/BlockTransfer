package ca.mcpnet.blocktransfer.utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFileTransport;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BTBlock;
import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;
import ca.mcpnet.blocktransfer.BlockTransferService.Client;
import ca.mcpnet.blocktransfer.BlockTransferService.Iface;

public class FetchItemIdMap {

	public static void main(String[] args) throws TException, FileNotFoundException {
		TTransport transport;
		
		transport = new TFramedTransport(new TSocket("10.10.10.5",9090));
		transport.open();
		TProtocol protocol = new TBinaryProtocol(transport);
		BlockTransferService.Client client = new BlockTransferService.Client(protocol);
		
		System.out.print("Connect...");
		Map<Integer, String> itemidmap = client.getItemIdMap();
		System.out.print("Save...");
		json.saveIdMap(itemidmap, "Direwolf.ItemIdMap.json");
		System.out.println("Done.");		
	}

}
