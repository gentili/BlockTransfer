package ca.mcpnet.blocktransfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		// Lets get a list of players per world
		
		return list;
	}
	
}
