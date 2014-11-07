package ca.mcpnet.blocktransfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

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
	
	@Override
	public Map<String, Integer> getBlockNameMap() throws TException {
		return BlockTransferMod.instance.getBlockNameMap();
	}

	@Override
	public List<BTPlayer> getPlayerList() throws TException {
		List<BTPlayer> list = new ArrayList<BTPlayer>();
        // Lets get a list of worlds
		for (int i = 0; i < MinecraftServer.getServer().worldServers.length; i++) {
			WorldServer world = MinecraftServer.getServer().worldServers[i];
			// Get the players
			Iterator pitr = world.playerEntities.iterator();
			while (pitr.hasNext()) {
				EntityPlayer player = (EntityPlayer) pitr.next();
				list.add(new BTPlayer(player.getEntityId(),
						player.getDisplayName(),
						world.provider.dimensionId,
						new BTdLocation(player.posX,player.posY,player.posZ)));
			}
		}
		
		return list;
	}

	@Override
	public void setBlock(int worldid, BTiLocation location, int id, int metadata)
			throws TException {
		WorldServer world = getWorld(worldid);
		if (world == null) {
			throw new TException("Invalid worldid specified");
		}
		world.setBlock(location.x, location.y, location.z, Block.getBlockById(id), metadata, 1+2);
	}
	
	private WorldServer getWorld(int worldid) {
		for (int i = 0; i < MinecraftServer.getServer().worldServers.length; i++) {
			WorldServer world = MinecraftServer.getServer().worldServers[i];
			if (world.provider.dimensionId == worldid)
				return world;
		}
		return null;
	}

}
