package ca.mcpnet.blocktransfer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import ca.mcpnet.blocktransfer.BTBlock;
import ca.mcpnet.blocktransfer.BTPlayer;
import ca.mcpnet.blocktransfer.BTWorldFrame;
import ca.mcpnet.blocktransfer.BTiVector;
import ca.mcpnet.blocktransfer.BlockTransferService;

public class Devastate {

	static final int RADIUS = 200;
	static final int FRAMEWIDTH = 10;
	
	public static void main(String[] args) throws TException, IOException {
		ArrayList<BTBlock> blocklist = new ArrayList<BTBlock>();
		blocklist.add(new BTBlock(7,0));
		/*
		blocklist.add(new BTBlock(1329,6));
		blocklist.add(new BTBlock(1347,5));
		blocklist.add(new BTBlock(1091,10));
		blocklist.add(new BTBlock(1132,13));
		blocklist.add(new BTBlock(1427,4));
		blocklist.add(new BTBlock(1427,5));
		blocklist.add(new BTBlock(1427,6));
		blocklist.add(new BTBlock(1428,4));
		blocklist.add(new BTBlock(1428,5));
		blocklist.add(new BTBlock(1428,6));
		blocklist.add(new BTBlock(244,0));
		blocklist.add(new BTBlock(244,1));
		blocklist.add(new BTBlock(244,2));
		*/		

		TFramedTransport src_transport = new TFramedTransport(new TSocket("localhost",9090));
		src_transport.open();
		BlockTransferService.Client src_client = new BlockTransferService.Client(new TBinaryProtocol(src_transport));
		List<BTPlayer> src_plyrs = src_client.getPlayerList();
		BTiVector playerloc = null;
		BTPlayer player = null;
		player = Translate.getPlayer(src_client.getPlayerList(),"Player980");
		playerloc = Translate.dloc2iloc(player.location);
		BTiVector locus = playerloc.deepCopy();
		locus.y += RADIUS - 10;
		int FRAMEHEIGHT = locus.y - (playerloc.y - 15);
		BTiVector framesize = new BTiVector(FRAMEWIDTH,FRAMEHEIGHT,FRAMEWIDTH);
		Random random = new java.util.Random();
		for (int x = 0; x < RADIUS*2 / FRAMEWIDTH;x++)
			for (int z = 0; z < RADIUS*2 / FRAMEWIDTH;z++) {
				BTiVector curloc = playerloc.deepCopy();
				curloc.x -= RADIUS - x * FRAMEWIDTH;
				curloc.z -= RADIUS - z * FRAMEWIDTH;
				curloc.y = playerloc.y - 15;
				
				if ((distance(locus, curloc,0,RADIUS,0) > RADIUS) &&
						(distance(locus, curloc,FRAMEWIDTH,FRAMEHEIGHT,FRAMEWIDTH) > RADIUS)) {
					System.out.println("Skipping frame "+x+","+z);
					continue;
				}
					
				System.out.println("Processing frame "+x+","+z);

				BTWorldFrame frame = src_client.getFrame(0, curloc, framesize);
				// Source blocks
				DataInputStream is = new DataInputStream(new ByteArrayInputStream(frame.getBlockdata()));
				// Dst blocks
				ByteArrayOutputStream ba = new ByteArrayOutputStream();
				DataOutputStream os = new DataOutputStream(ba);
				for (int fx = 0;fx < frame.size.x;fx++) {
					for (int fy = 0;fy < frame.size.y;fy++) {						
						for (int fz = 0;fz < frame.size.z;fz++) {
							int src_blkid = is.readShort();
							int metadata = is.readByte();
							random.setSeed(100000*(fz+curloc.z) * (fx+curloc.x));
							int randrad = RADIUS + random.nextInt(2);
							int dist = distance(locus, curloc, fx, fy, fz);
							if (dist < randrad) {
								src_blkid = 0;
								metadata = 0;
							} else if ((dist < randrad + 4) && (src_blkid != 0)) {
								BTBlock block = blocklist.get(random.nextInt(blocklist.size()));
								src_blkid = block.id;
								metadata = block.metadata;
							}
							
							os.writeShort(src_blkid);
							os.writeByte(metadata);
						}
					}
				}
				os.close();
				frame.setBlockdata(ba.toByteArray());
				src_client.putFrame(0, curloc, frame);
				
				}
			
		
		// BTWorldFrame frame = src_client.getFrame(player.getWorldid(), iloc, isize);
		
	}

	public static int distance(BTiVector locus, BTiVector curloc, int fx,
			int fy, int fz) {
		int xd = locus.x - (curloc.x + fx);
		int yd = locus.y - (curloc.y + fy);
		int zd = locus.z - (curloc.z + fz);
		int dist = (int) Math.sqrt(xd*xd + yd*yd + zd*zd);
		return dist;
	}

}
