package client;

import constants.Commands;
import constants.Status;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import objects.FileData;
import network.NetworkAnswer;

@Slf4j
public class ClientConnectionHandler extends ChannelInboundHandlerAdapter {
    private TableView<FileData> filesOnServerTable;
    private Client parentHandle;

    public ClientConnectionHandler(Client client) {
        parentHandle = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetworkAnswer answer = (NetworkAnswer) msg;
        System.out.println(answer.toString());
        if (answer.getQuestionMessageType().equals(Commands.AUTHORISATION)) {
            if (answer.getAnswer() != null) {
                if (answer.getAnswer().equals(Status.OK)) {
                    parentHandle.setUserToken(answer.getUid());
                    parentHandle.setConnected(true);
                }
                if (answer.getAnswer().equals(Status.DENIED)) {
                    parentHandle.setUserToken(null);
                    parentHandle.setConnected(false);
                }

            }

        }
        if (parentHandle.isConnected()) {
            if (answer.getQuestionMessageType().equals(Commands.GET_FILE_LIST)) {
                if (answer.getCurrentPart() == 0) {
                    filesOnServerTable.getItems().clear();
                }
                filesOnServerTable.getItems().add((FileData) answer.getAnswer());

            }
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        parentHandle.setConnected(false);
        log.error(cause.getMessage());


    }

    public void setFileListContainer(TableView<FileData> container) {
        filesOnServerTable = container;
    }
}