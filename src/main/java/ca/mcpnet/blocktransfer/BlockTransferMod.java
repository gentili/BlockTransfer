package ca.mcpnet.blocktransfer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;

import ca.mcpnet.blocktransfer.BlockTransferService.Processor;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

/*
 * This is the main Mod Class
 */ 

@Mod(modid = BlockTransferMod.MODID, name = BlockTransferMod.MODNAME, version = BlockTransferMod.VERSION)
public class BlockTransferMod
{
    public static final String MODID = "blocktransfer";
    public static final String MODNAME = "Block Transfer Mod";
    public static final String VERSION = "1.1.0";
    
    @Instance(value = BlockTransferMod.MODID)
    public static BlockTransferMod instance;
    
	public static Logger log;

	private Thread BTserverthread;
	private TBlockTransferServer BTserver;
	private Map<Integer, String> blockidmap;
	private Map<String, Integer> blocknamemap;
	private Map<Integer, String> itemidmap;
	private Map<String, Integer> itemnamemap;
	
	public Map<Integer, String> getBlockIdMap() {
		checkBlockMaps();
		return blockidmap;
	}
	
	public Map<String, Integer> getBlockNameMap() {
		checkBlockMaps();
		return blocknamemap;

	}
	
	private void checkBlockMaps() {
		if (blockidmap != null && blocknamemap != null)
			return;
		log.info("Initializing block maps");
		// Build the block mapping list
		blockidmap = new HashMap<Integer, String>();
		blocknamemap = new HashMap<String, Integer>();
		for (Iterator bitr = Block.blockRegistry.iterator();bitr.hasNext();) {
			Block block = (Block) bitr.next();
			String blockname = Block.blockRegistry.getNameForObject(block);
			int blockid = Block.blockRegistry.getIDForObject(block);
			log.info("Adding block " + blockid + "->" + blockname);
			blockidmap.put(blockid, blockname);
			blocknamemap.put(blockname, blockid);
		}
	}
	
	public Map<Integer, String> getItemIdMap() {
		checkItemMaps();
		return itemidmap;
	}
    
	public Map<String, Integer> getItemNameMap() {
		checkItemMaps();
		return itemnamemap;
	}

	private void checkItemMaps() {
		if (itemidmap != null && itemnamemap != null)
			return;
		log.info("Initializing item maps");
		// Build the item mapping list
		itemidmap = new HashMap<Integer, String>();
		itemnamemap = new HashMap<String, Integer>();
		for (Iterator iitr = Item.itemRegistry.iterator();iitr.hasNext();) {
			Item item = (Item) iitr.next();
			String itemname = Item.itemRegistry.getNameForObject(item);
			int itemid = Item.itemRegistry.getIDForObject(item);
			log.info("Adding item " + itemid + "->" + itemname);
			itemidmap.put(itemid, itemname);
			itemnamemap.put(itemname, itemid);
		}
	}
	/*
	 * The following are Forge and FML specific methods
	 */
	@NetworkCheckHandler
	public static boolean networkCheckHandler(Map<String, String> map, Side side) {
		return true;
	}

	@EventHandler
	public void onFMLPreInitializationEvent(FMLPreInitializationEvent e) {
		log = e.getModLog();
		log.info("Initialization deferred to Server Start and first requests");
	}
	
	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent e) {
		
	}
        
    @EventHandler
    public void onFMLServerStartedEvent(FMLServerStartedEvent e) {
    	log.info("Starting BlockTransfer server on port 9090");
    	
		BlockTransferServiceHandler handler = new BlockTransferServiceHandler();
		Processor processor = new BlockTransferService.Processor(handler);

		try {
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(
					9090);
			TNonblockingServer.Args serverArgs = new TNonblockingServer.Args(serverTransport);
			serverArgs.processor(processor);
			serverArgs.transportFactory(new TFramedTransport.Factory());
			serverArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
			BTserver = new TBlockTransferServer(serverArgs);
			BTserverthread = new Thread() {
				public void run() {
					BTserver.serve();
				}
			};
			BTserverthread.start();
		} catch (Exception ex) {
			throw new RuntimeException("Unable to start BlockTransfer server",ex);
		}
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @EventHandler
    public void onFMLServerStoppingEvent(FMLServerStoppingEvent e) {
    	log.info("Stopping BlockTransfer server");
    	MinecraftForge.EVENT_BUS.unregister(this);
        FMLCommonHandler.instance().bus().unregister(this);
        BTserver.stop();
        try {
			BTserverthread.join();
		} catch (InterruptedException ex) {
			throw new RuntimeException("Interrupted during BlockTransfer server shutdown",ex);
		}
    }
    
    @SubscribeEvent
    public void handle(TickEvent e) {
    	// Need to handle requests in -the context of the main
    	// server thread as they may request info from the world
    	// or modify the world
    	if (e.type == TickEvent.Type.SERVER)
    		if (e.phase == TickEvent.Phase.START)
    			BTserver.serviceRequestQueue();
    }

    /*
    @SubscribeEvent
    public void handle(PlayerInteractEvent e) {
    	if (e.action != e.action.LEFT_CLICK_BLOCK) {
    		return;
    	}
    	e.world.func_147480_a(e.x, e.y, e.z,true);
    }
    
    @SubscribeEvent
    public void onChunkEvent(ChunkEvent e) {
    	if (e.getChunk().xPosition > 500)
    		log.info(e.getClass().getName()+" "+e.getChunk().xPosition+" : "+e.getChunk().zPosition);
    }
    */

}
