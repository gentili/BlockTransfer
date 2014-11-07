namespace java ca.mcpnet.blocktransfer


typedef map<i32,string> BlockIdMap

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

typedef list<BTPlayer> PlayerList

service BlockTransferService
{
    string getVersion()

    BlockIdMap getBlockIdMap()

    PlayerList getPlayerList()
}
