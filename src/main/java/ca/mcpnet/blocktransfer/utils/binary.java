package ca.mcpnet.blocktransfer.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFileTransport;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BlockTransferService.getBlockIdMap_result;
import ca.mcpnet.blocktransfer.BlockTransferService.getFrame_result;

public final class binary {
	
	private binary() { } // Prevent instantiation
	
	public static void saveFrame(BTWorldFrame frame, String filename)
			throws TTransportException, TException, IOException {
		TIOStreamTransport transport = new TIOStreamTransport(
				new FileOutputStream(filename));
		transport.open();
		TProtocol protocol = new TBinaryProtocol(transport);
		getFrame_result getFrame_result = new getFrame_result();
		getFrame_result.success = frame;
		getFrame_result.write(protocol);
		transport.close();
	}
	
	public static BTWorldFrame loadFrame(String filename) throws FileNotFoundException, TException {
		TIOStreamTransport transport = new TIOStreamTransport(
				new FileInputStream(filename));
		transport.open();
		TProtocol protocol = new TBinaryProtocol(transport);
		getFrame_result getFrame_result = new getFrame_result();
		getFrame_result.read(protocol);
		return getFrame_result.success;
	}

}
