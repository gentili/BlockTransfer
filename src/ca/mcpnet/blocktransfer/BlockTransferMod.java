package ca.mcpnet.blocktransfer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;

import ca.mcpnet.blocktransfer.BlockTransferService.Processor;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;

@Mod(modid = BlockTransferMod.MODID, name = BlockTransferMod.MODNAME, version = BlockTransferMod.VERSION)
public class BlockTransferMod
{
    public static final String MODID = "blocktransfer";
    public static final String MODNAME = "Block Transfer Mod";
    public static final String VERSION = "1.0";
    
    @Instance(value = BlockTransferMod.MODID)
    public static BlockTransferMod instance;
    
	public static java.util.logging.Logger log;

	private Thread BTserverthread;
	private TBlockTransferServer BTserver;
	private Map<Integer, String> blockidmap;
	private Map<String, Integer> blocknamemap;
	
	public Map<Integer, String> getBlockIdMap() {
		return blockidmap;
	}
	
	public Map<String, Integer> getBlockNameMap() {
		return blocknamemap;
	}

	/*
	 * The following are Forge and FML specific methods
	 */

	@PreInit
	public void onFMLPreInitializationEvent(FMLPreInitializationEvent e) {
		log = e.getModLog();
		log.info("PreInitialization");
	}
	
	@PostInit
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent e) {
		// Build the block mapping list
		blockidmap = new HashMap<Integer, String>();
		blocknamemap = new HashMap<String, Integer>();
		for (int i = 0; i < Block.blocksList.length;i++) {
			Block block = Block.blocksList[i];
			
			if (block == null)
				continue;
			if (block.getBlockName() == null)
				continue;
			String blockname = block.getBlockName();
			int blockid = block.blockID;
			log.info("Adding map " + blockid + "->" + blockname);
			blockidmap.put(blockid, blockname);
			blocknamemap.put(blockname, blockid);
		}
	}
        
    @ServerStarted
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
        // FMLCommonHandler.instance().bus().register(this);
    }
    /*
    @ServerStopping
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
		BTserver.serviceRequestQueue();
    }
    */
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