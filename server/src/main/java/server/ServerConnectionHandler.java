package server;

import constants.Commands;
import constants.Status;
import filesystem.FileInfo;
import filesystem.FileNavigator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import network.FileContent;
import network.NetworkAnswer;
import network.NetworkMessage;
import network.UserData;
import objects.*;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ChannelHandler.Sharable
public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {

    private Map<UUID, FileNavigator> usersPlacement;

    public void setUsersPlacement(Map<UUID, FileNavigator> usersPlacement) {
        this.usersPlacement = usersPlacement;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("Recieved");
        NetworkMessage nm = (NetworkMessage) msg;
        messageDispatch(nm, ctx);
    }

    private void messageDispatch(NetworkMessage nm, ChannelHandlerContext ctx) {

        FileNavigator fn = null;

        if (!nm.getMessagePurpose().equals(Commands.AUTHORISATION)) {
            fn = usersPlacement.get(nm.getUid());
        }


        if (nm.getMessagePurpose() == Commands.AUTHORISATION) {
            NetworkAnswer answer = new NetworkAnswer();
            answer.setQuestionMessageType(nm.getMessagePurpose());

            UserData ud = (UserData) nm;


            //TODO добавить авторизацию по базе

            if (usersPlacement != null) {
                UUID uid = UUID.randomUUID();
                usersPlacement.put(uid, new FileNavigator());
                answer.setUid(uid);
                answer.setAnswer(Status.OK);

            } else {
                answer.setAnswer(Status.DENIED);
            }
            ctx.writeAndFlush(answer);

        } else if (nm.getMessagePurpose() == Commands.GET_FILE_LIST && fn != null) {

            try {
                String s = nm.getExtraInfo();

                if (s != null) {
                    if (!s.equals("") && !s.equals("..")) {
                        fn.goInto(s);
                    } else if (s.equals("..")) {
                        fn.goUp();
                    }
                }

                List<FileInfo> fileInfo = fn.getFilesListFromCurrent();
                int totalParts = fileInfo.size() + (fn.isOnTop() ? 0 : 1);
                NetworkAnswer na = new NetworkAnswer<FileData>(totalParts);
                na.setUid(nm.getUid());

                if (!fn.isOnTop()) {
                    na.setQuestionMessageType(nm.getMessagePurpose());
                    na.setAnswer(new FileData("..", -2, true));
                    ctx.writeAndFlush(na);
                }

                int partnum = 0 + (fn.isOnTop() ? 0 : 1);

                for (FileInfo f : fileInfo) {
                    na.setQuestionMessageType(nm.getMessagePurpose());
                    na.setAnswer(new FileData(f.getFileName(), f.getFileSize(), f.isFolder()));
                    na.setCurrentPart(partnum++);
                    ctx.writeAndFlush(na);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (nm.getMessagePurpose() == Commands.FILE_DATA && fn != null) {
            FileContent fc = (FileContent) nm;
            System.out.println("Need to upload " + fc.getFileName());
            System.out.println(fc);
            fn.createAndWriteToFileOnCurrent(fc.getFileName(),fc.getFileContent());

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");

    }
}
