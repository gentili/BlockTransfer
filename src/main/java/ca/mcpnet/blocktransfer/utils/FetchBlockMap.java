package ca.mcpnet.blocktransfer.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFileTransport;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;
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
import ca.mcpnet.blocktransfer.BlockTransferService.getBlockIdMap_result;

public class FetchBlockMap {

	public static void main(String[] args) {
		TTransport transport;
		
		try {
			transport = new TFramedTransport(new TSocket("direwolf.mcpnet.ca",9090));
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			BlockTransferService.Client client = new BlockTransferService.Client(protocol);
			
			System.out.print("Connect...");
			Map<Integer, String> blockidmap = client.getBlockIdMap();
			
			TIOStreamTransport JSONtransport = new TIOStreamTransport(
					new FileOutputStream("BlockMap.Direwolf.json"));
			JSONtransport.open();
			TJSONProtocol JSONprotocol = new TJSONProtocol(JSONtransport);
			BlockTransferService service = new BlockTransferService();
			getBlockIdMap_result blockidmap_result = new getBlockIdMap_result();
			blockidmap_result.success = blockidmap;
			blockidmap_result.write(JSONprotocol);
			JSONtransport.close();
			System.out.println("Done.");
			
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
