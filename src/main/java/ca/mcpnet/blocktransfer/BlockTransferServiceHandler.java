package ca.mcpnet.blocktransfer;

import java.util.Map;

import org.apache.thrift.TException;

public class BlockTransferServiceHandler implements BlockTransferService.Iface {

	@Override
	public String getVersion() throws TException {
		return BlockTransferMod.VERSION;
	}

	@Override
	public Map<Integer, String> getBlockIdMap() throws TException {
		return BlockTransferMod.instance.getBlockIdMap();
	}
	
}
