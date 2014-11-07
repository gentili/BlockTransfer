package ca.mcpnet.blocktransfer;

import org.apache.thrift.TException;

public class BlockTransferServiceHandler implements BlockTransferService.Iface {

	@Override
	public void ping() throws TException {
	}

	@Override
	public String echo(String message) throws TException {
		return message;
	}

}
