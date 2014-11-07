package ca.mcpnet.blocktransfer;

import org.apache.thrift.TException;

public class BlockTransferServiceHandler implements BlockTransferService.Iface {

	@Override
	public String getVersion() throws TException {
		return BlockTransferMod.VERSION;
	}

}
