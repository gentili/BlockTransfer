package ca.mcpnet.blocktransfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;

import org.apache.thrift.TException;

public class BlockTransferServiceHandler implements BlockTransferService.Iface {

	private WorldServer getWorld(int worldid) throws TException {
		for (int i = 0; i < MinecraftServer.getServer().worldServers.length; i++) {
			WorldServer world = MinecraftServer.getServer().worldServers[i];
			if (world.provider.dimensionId == worldid)
				return world;
		}
		throw new TException("Invalid worldid specified");
	}
	
	private BTTileEntity TileEntity2BT(BTiVector offset, TileEntity tile) throws TException {
		NBTTagCompound nbt = new NBTTagCompound();
		tile.writeToNBT(nbt);
		ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
		try {
			CompressedStreamTools.write(nbt, new DataOutputStream(bytearray));
		} catch (IOException e) {
			throw new TException(e);
		}
		return new BTTileEntity(new BTiVector(tile.xCoord-offset.x,tile.yCoord-offset.y,tile.zCoord-offset.z),
				ByteBuffer.wrap(bytearray.toByteArray()));
	}
	
	/*
	 * This is where the service endpoint handler functions start 
	 */

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
						new BTdVector(player.posX,player.posY,player.posZ)));
			}
		}
		
		return list;
	}

	@Override
	public void setBlock(int worldid, BTiVector location, BTBlock block)
			throws TException {
		WorldServer world = getWorld(worldid);
		world.setBlock(location.x, location.y, location.z, Block.getBlockById(block.getId()), block.getMetadata(), 1+2);
	}
	
	@Override
	public BTBlock getBlock(int worldid, BTiVector location)
			throws TException {
		WorldServer world = getWorld(worldid);
		Block block = world.getBlock(location.x, location.y, location.z);
		return new BTBlock(Block.getIdFromBlock(block),world.getBlockMetadata(location.x, location.y, location.z));
	}

	@Override
	public BTWorldFrame getFrame(int worldid, BTiVector location,
			BTiVector size) throws TException {
		WorldServer world = getWorld(worldid);

		// Build the block byte buffers
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(ba);
		try {
			for (int x = location.x;x < location.x+size.x;x++)
				for (int y = location.y;y < location.y+size.y;y++)
					for (int z = location.z;z < location.z+size.z;z++) {
							os.writeShort(Block.getIdFromBlock(world.getBlock(x, y, z)));
							os.writeByte(world.getBlockMetadata(x, y, z));
					}
		} catch (IOException e) {
			throw new TException(e);
		}
		
		BTWorldFrame frame = new BTWorldFrame(size,
				ByteBuffer.wrap(ba.toByteArray()),
				new ArrayList<BTTileEntity>());
		// Grab the tile entities in the box
		List tileEntityList = world.func_147486_a(location.x, location.y, location.z, 
				location.x+size.x, location.y+size.y, location.z+size.z);
		for (Iterator itr = tileEntityList.iterator(); itr.hasNext();) {
			BTTileEntity tile = TileEntity2BT(location,(TileEntity) itr.next());
			frame.addToTilelist(tile);
		}
		return frame;
	}

	@Override
	public void putFrame(int worldid, BTiVector location, BTWorldFrame frame)
			throws TException {
		BTiVector size = frame.getSize();
		WorldServer world = getWorld(worldid);

		// Build the block byte buffers
		ByteArrayInputStream ba = new ByteArrayInputStream(frame.getBlockdata());
		DataInputStream is = new DataInputStream(ba);
		try {
			for (int x = location.x;x < location.x+size.x;x++)
				for (int y = location.y;y < location.y+size.y;y++)
					for (int z = location.z;z < location.z+size.z;z++) {
						world.setBlock(x, y, z, Block.getBlockById(is.readShort()), is.readByte(), 1+2);
					}
		} catch (IOException e) {
			throw new TException(e);
		}
		
	}

}
