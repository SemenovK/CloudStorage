package server;

import constants.Commands;
import filesystem.FileNavigator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import network.FileContent;
import network.NetworkMessage;


import java.util.Map;
import java.util.UUID;

@Slf4j
@ChannelHandler.Sharable
public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {


    private Server parentHandler;
    private DBService dbService;

    private Map<UUID, FileNavigator> usersPlacement;

    public ServerConnectionHandler(Server parentHandler) {
        this.parentHandler = parentHandler;
        this.usersPlacement = parentHandler.getUsersPlacement();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        NetworkMessage nm = (NetworkMessage) msg;
        log.trace("Recieved: ", nm);
        messageDispatch(nm, ctx);
    }

    private void messageDispatch(NetworkMessage nm, ChannelHandlerContext ctx) {

        FileNavigator fn = null;

        if (!nm.getMessagePurpose().equals(Commands.AUTHORISATION)) {
            fn = usersPlacement.get(nm.getUid());
        }


        System.out.println(nm);
        if (nm.getMessagePurpose() == Commands.AUTHORISATION) {
            parentHandler.authUser(nm);
        } else if (nm.getMessagePurpose() == Commands.GET_FILE_LIST) {
            parentHandler.createFileList(nm);

        } else if (nm.getMessagePurpose() == Commands.FILE_DATA && fn != null) {
            FileContent fc = (FileContent) nm;
            fn.putFileToQueue(fc);

        } else if (nm.getMessagePurpose() == Commands.FILE_OVERWRITE && fn != null) {
            parentHandler.fileWritingDecision(nm.getExtraInfo(), nm.getUid(), nm.getStatus());

        } else if (nm.getMessagePurpose() == Commands.CREATE_NEW_FOLDER && fn != null) {
            fn.createFolder(nm.getExtraInfo());

        } else if (nm.getMessagePurpose() == Commands.DELETE && fn != null) {
            fn.deleteFileOrFolder(nm.getExtraInfo());

        } else if (nm.getMessagePurpose() == Commands.FILE_DOWNLOAD && fn != null) {
            parentHandler.sendFile(nm.getExtraInfo(), nm.getUid());

        } else if (nm.getMessagePurpose() == Commands.GET_USERS_LIST) {
            parentHandler.createUsersList(nm.getUid());

        } else if (nm.getMessagePurpose() == Commands.GET_FRIENDS_LIST) {
            parentHandler.createFriendsList(nm.getUid());

        } else if (nm.getMessagePurpose() == Commands.SHARE_FOLDER) {
            parentHandler.shareFolderManage(nm);
        }
        else if (nm.getMessagePurpose() == Commands.GET_MY_USERSHARES_LIST) {
            parentHandler.collectUserShares(nm);
        } else if (nm.getMessagePurpose() == Commands.GO_INTO_SHARED_MODE) {
            parentHandler.goInShareMode(nm);

        }else if (nm.getMessagePurpose() == Commands.GO_INTO_NORMAL_MODE) {
            parentHandler.goToNormal(nm);
        } else if (nm.getMessagePurpose() == Commands.GET_FILE_SHAREDLIST) {
            parentHandler.getSharedFileList(nm);

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");

    }

    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }
}
