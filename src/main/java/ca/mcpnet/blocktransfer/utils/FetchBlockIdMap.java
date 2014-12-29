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

public class FetchBlockIdMap {

	public static void main(String[] args) throws FileNotFoundException, TTransportException, TException {
		TTransport transport;
		
		transport = new TFramedTransport(new TSocket("mcpnet.ca",9090));
		System.out.print("Connect...");
		transport.open();
		TProtocol protocol = new TBinaryProtocol(transport);
		BlockTransferService.Client client = new BlockTransferService.Client(protocol);
		
		Map<Integer, String> blockidmap = client.getBlockIdMap();
		System.out.print("Save...");
		json.saveIdMap(blockidmap, "Mindcrack.BlockIdMap.json");
		System.out.println("Done.");		
	}

}
