namespace java ca.mcpnet.blocktransfer

struct BTdLocation {
    1:required double x;
    2:required double y;
    3:required double z;
}

struct BTiLocation {
    1:required i32 x;
    2:required i32 y;
    3:required i32 z;
}

struct BTPlayer {
    1:required i32 id;
    2:required string name;
    3:required i32 worldid;
    4:required BTdLocation location;
}

struct BTBlock {
    1:required i32 id;
    2:required i32 metadata;
}

struct BTWorldFrame {
    1:required i32 xlength;
    2:required i32 ylength;
    3:required i32 zlength;
    4:required binary ids;
    5:required binary metadata;
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

    void setBlock(1:i32 worldid 2:BTiLocation location, 3:BTBlock block)

    BTBlock getBlock(1:i32 worldid 2:BTiLocation location)

    BTWorldFrame getFrame(1:i32 worldid 2:BTiLocation location, 3:BTiLocation size)

}
