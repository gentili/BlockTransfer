namespace java ca.mcpnet.blocktransfer

struct BTdVector {
    1:required double x;
    2:required double y;
    3:required double z;
}

struct BTiVector {
    1:required i32 x;
    2:required i32 y;
    3:required i32 z;
}

struct BTPlayer {
    1:required i32 id;
    2:required string name;
    3:required i32 worldid;
    4:required BTdVector location;
}

struct BTBlock {
    1:required i32 id;
    2:required i32 metadata;
}

struct BTTileEntity {
    1:required BTiVector location;
    2:required binary nbt;
}

struct BTWorldFrame {
    1:required BTiVector size;
    2:required binary blockdata;
    3:required list<BTTileEntity> tilelist;
}

typedef map<i32,string> BlockIdMap
typedef map<string,i32> BlockNameMap
typedef list<BTPlayer> PlayerList

service BlockTransferService
{
    string getVersion()

    BlockIdMap getBlockIdMap()

    BlockNameMap getBlockNameMap()

    PlayerList getPlayerList()

    void setBlock(1:i32 worldid 2:BTiVector location, 3:BTBlock block)

    BTBlock getBlock(1:i32 worldid 2:BTiVector location)

    BTWorldFrame getFrame(1:i32 worldid 2:BTiVector location, 3:BTiVector size)

    void putFrame(1:i32 worldid 2:BTiVector location, 3:BTWorldFrame frame)

}
