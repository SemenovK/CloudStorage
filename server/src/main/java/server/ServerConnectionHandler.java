package server;

import constants.Commands;
import filesystem.FileInfo;
import filesystem.FileNavigator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import objects.*;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("Recieved");
        NetworkMessage nm = (NetworkMessage) msg;
        messageDispatch(nm, ctx);
    }

    private void messageDispatch(NetworkMessage nm, ChannelHandlerContext ctx) {

        if (nm.getMessagePurpose() == Commands.AUTHORISATION) {
            NetworkAnswer answer = new NetworkAnswer();
            answer.setQuestionMessageType(nm.getMessagePurpose());

            UserData ud = (UserData) nm;
            System.out.println(ud);
            answer.setAnswer("OK");
            //TODO добавить авторизацию по базе
            ctx.writeAndFlush(answer);

        } else if (nm.getMessagePurpose() == Commands.GET_FILE_LIST) {

            try {
                String s = nm.getExtraInfo();
                Path p;
                if(s == "" || s==null){
                    p = Paths.get("C:", "TMP");
                } else {
                    p = Paths.get("C:", "TMP", s);
                }


                System.out.println(p);
                FileNavigator fn = new FileNavigator(p.toUri());
                List<FileInfo> fileInfo = fn.getFilesListFromCurrent();
                NetworkAnswer na = new NetworkAnswer<FileData>(fileInfo.size());
                int partnum = 0;
                for (FileInfo f : fileInfo) {
                    na.setQuestionMessageType(nm.getMessagePurpose());
                    na.setAnswer(new FileData(f.getFileName(), f.getFileSize(), f.isFolder()));
                    na.setCurrentPart(partnum++);
                    ctx.writeAndFlush(na);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");

    }
}
