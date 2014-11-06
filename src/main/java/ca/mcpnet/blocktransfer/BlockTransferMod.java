package ca.mcpnet.blocktransfer;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = BlockTransferMod.MODID, version = BlockTransferMod.VERSION)
public class BlockTransferMod
{
    public static final String MODID = "blocktransfer";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void handle(PlayerInteractEvent e) {
    	if (e.action != e.action.LEFT_CLICK_BLOCK) {
    		return;
    	}
    	System.out.println(e.entityPlayer.getDisplayName()+
    			" "+e.action.toString()+
    			" "+e.x+" "+e.y+" "+e.z);
    	// e.world.setBlockToAir(e.x, e.y, e.z);
    	e.world.createExplosion(null, e.x, e.y, e.z, 1.0f, true);
    	Block block = e.world.getBlock(e.x, e.y, e.z);
    }
}
