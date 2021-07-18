package server;

import constants.Commands;
import filesystem.FileNavigator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import objects.FileList;
import objects.NetworkAnswer;
import objects.NetworkMessage;
import objects.UserData;


import java.nio.file.Paths;

@Slf4j
public class InboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected");
    }

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
            System.out.println("Wanted file list");
            try {
                FileNavigator fn = new FileNavigator(Paths.get("C:", "TMP").toUri());
                FileList fl = new FileList(fn.getFilesListFromCurrent());

                NetworkAnswer<FileList> na = new NetworkAnswer<>();
                na.setQuestionMessageType(nm.getMessagePurpose());
                na.setAnswer(fl);
                ctx.writeAndFlush(na);

                System.out.println(na);

                NetworkAnswer answer = new NetworkAnswer();
                answer.setQuestionMessageType(nm.getMessagePurpose());
                ctx.writeAndFlush(answer);


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
