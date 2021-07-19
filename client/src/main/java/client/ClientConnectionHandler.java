package client;

import constants.Commands;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.scene.control.TableView;
import objects.FileData;
import objects.NetworkAnswer;


public class ClientConnectionHandler extends ChannelInboundHandlerAdapter {
    private TableView<FileData> filesOnServerTable;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetworkAnswer answer = (NetworkAnswer) msg;
        if (answer.getQuestionMessageType().equals(Commands.GET_FILE_LIST)) {
            if (answer.getCurrentPart() == 0) {
                filesOnServerTable.getItems().clear();
            }
            filesOnServerTable.getItems().add((FileData) answer.getAnswer());

        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public void setFileListContainer(TableView<FileData> container) {
        filesOnServerTable = container;
    }
}
