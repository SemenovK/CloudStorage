package client;

import constants.Commands;
import constants.Status;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import network.FileContent;
import network.NetworkMessage;
import objects.FileData;
import network.NetworkAnswer;
import objects.UserInfo;

@Slf4j
public class ClientConnectionHandler extends ChannelInboundHandlerAdapter {

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
                    parentHandle.setAuthorised(true);


                }
                if (answer.getAnswer().equals(Status.DENIED)) {
                    parentHandle.setUserToken(null);
                    parentHandle.setConnected(false);
                    parentHandle.setAuthorised(false);
                }

            }

        }
        if (parentHandle.isConnected()) {
            if (answer.getQuestionMessageType().equals(Commands.GET_FILE_LIST)
                    || answer.getQuestionMessageType().equals(Commands.GO_INTO_SHARED_MODE)
                    || answer.getQuestionMessageType().equals(Commands.GET_FILE_SHAREDLIST)) {
                if (answer.getCurrentPart() == 0) {
                    parentHandle.getObservableFileList().clear();
                }
                parentHandle.getObservableFileList().add((FileData) answer.getAnswer());

            } else if (answer.getQuestionMessageType().equals(Commands.FILE_DATA)) {
                Status s = (Status) answer.getAnswer();
                if (s.equals(Status.FILE_EXISTS)) {
                    String fileName = answer.getExtraInfo();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "File " + fileName + " already exists.\nOverwrite?", ButtonType.YES, ButtonType.NO);
                        alert.showAndWait();
                        NetworkMessage nm = new NetworkMessage(Commands.FILE_OVERWRITE);
                        nm.setExtraInfo(fileName);
                        nm.setUid(answer.getUid());
                        nm.setStatus(alert.getResult().equals(ButtonType.YES) ? Status.OK : Status.CANCEL);
                        ctx.writeAndFlush(nm);
                    });

                }

            } else if (answer.getQuestionMessageType().equals(Commands.FILE_DOWNLOAD)) {
                FileContent fc = (FileContent) answer.getAnswer();
                parentHandle.writeFile(fc);

            } else if (answer.getQuestionMessageType().equals(Commands.GET_USERS_LIST)) {
                if (answer.getCurrentPart() == 0) {
                    parentHandle.getObservableUsersList().clear();
                }
                UserInfo ui = (UserInfo) answer.getAnswer();
                if (!ui.isThisUserIsCurrent())
                    parentHandle.getObservableUsersList().add(ui);

            } else if (answer.getQuestionMessageType().equals(Commands.GET_FRIENDS_LIST)) {
                    if (answer.getCurrentPart() == 0) {
                        //parentHandle.getObservableFriendsList().clear();
                    }
                    parentHandle.getObservableFriendsList().add((UserInfo) answer.getAnswer());


            } else if (answer.getQuestionMessageType().equals(Commands.GET_MY_USERSHARES_LIST)) {
                if (answer.getCurrentPart() == 0) {
                    parentHandle.getObservableSharedToUsersList().clear();
                }
                parentHandle.getObservableSharedToUsersList().add((UserInfo) answer.getAnswer());

            }
            UserInfo ui = new UserInfo(-1, "[USER_HOME]", true);
            if(!parentHandle.getObservableFriendsList().contains(ui)){
                parentHandle.getObservableFriendsList().add(ui);
            }

        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        parentHandle.setConnected(false);
        log.error(cause.getMessage());

    }


}
