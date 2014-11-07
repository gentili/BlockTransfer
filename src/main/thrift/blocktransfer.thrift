namespace java ca.mcpnet.blocktransfer

typedef map<i32,string> BlockIdMap

service BlockTransferService
{
    string getVersion()

    BlockIdMap getBlockIdMap()
}
