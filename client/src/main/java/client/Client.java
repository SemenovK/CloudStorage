package client;

import constants.Commands;
import constants.Status;
import filesystem.FileInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import network.FileContent;
import network.NetworkMessage;
import objects.FileData;
import objects.ShareFolderTo;
import objects.UserInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Client {


    private SocketChannel mySocketChannel;
    private EventLoopGroup group;
    private ClientConnectionHandler handler;
    private UUID userToken;
    private boolean connected;
    private Path pathToSave;
    private List<UserInfo> usersList;
    private ObservableList<UserInfo> observableUsersList;

    private List<UserInfo> sharedToUsersList;
    private ObservableList<UserInfo> observableSharedToUsersList;


    private List<UserInfo> friendsList;
    private ObservableList<UserInfo> observableFriendsList;

    private List<FileData> fileInfoList;
    private ObservableList<FileData> observableFileList ;


    public ObservableList<UserInfo> getObservableUsersList() {
        return observableUsersList;
    }
    public ObservableList<UserInfo> getObservableSharedToUsersList() {
        return observableSharedToUsersList;
    }

    public void setAuthorised(boolean authorised) {
        this.authorised = authorised;
    }

    public boolean isAuthorised() {
        return authorised;
    }

    private boolean authorised;

    public Path getPathToSave() {
        return pathToSave;
    }

    public void setPathToSave(Path pathToSave) {
        this.pathToSave = pathToSave;
    }

    public boolean isConnected() {
        return connected;
    }


    public void setUserToken(UUID userToken) {
        this.userToken = userToken;
    }

    protected SocketChannel getMySocketChannel() {
        return mySocketChannel;
    }

    public ClientConnectionHandler getHandler() {
        return handler;
    }


    public ObservableList<UserInfo> getObservableFriendsList() {
        return observableFriendsList;
    }

    public ObservableList<FileData> getObservableFileList() {
        return observableFileList;
    }

    public Client() {
        handler = new ClientConnectionHandler(this);
        connected = false;
        Thread thread = new Thread(() -> {
            group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel socketChannel) {
                                mySocketChannel = socketChannel;
                                ChannelPipeline channelPipeline = socketChannel.pipeline();
                                channelPipeline.addLast(new ObjectEncoder());
                                channelPipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                channelPipeline.addLast(handler);

                            }
                        })
                        .connect("localhost", 20115)
                        .sync()
                        .channel()
                        .closeFuture()
                        .sync();

            } catch (Exception e) {
                log.error("Connection fault:" + e.getLocalizedMessage());
            } finally {
                group.shutdownGracefully();
            }
        });

        thread.setDaemon(true);
        thread.start();
        usersList = new ArrayList<>();
        observableUsersList = FXCollections.observableList(usersList);

        sharedToUsersList = new ArrayList<>();
        observableSharedToUsersList = FXCollections.observableList(sharedToUsersList);


        friendsList = new ArrayList<>();
        observableFriendsList = FXCollections.observableList(friendsList);

        fileInfoList = new ArrayList<>();
        observableFileList = FXCollections.observableList(fileInfoList);
    }

    public void sendObject(NetworkMessage nm) {
        nm.setUid(this.userToken);
        System.out.println("Sent:" + nm);
        mySocketChannel.writeAndFlush(nm);
    }

    public void close() {
        if (group != null)
            group.shutdownGracefully();
        if (mySocketChannel != null)
            mySocketChannel.close();
    }

    public void setConnected(boolean b) {
        connected = b;
    }

    public void writeFile(FileContent fc) {
        if (getPathToSave().toFile().exists()) {
            try {
                String filePath = fc.getFilePath();
                if (filePath.equals("\\")) {
                    Files.write(Paths.get(getPathToSave().toString(), fc.getFileName()), fc.getFileContent());
                    //System.out.println(Paths.get(getPathToSave().toString(), filePath, fc.getFileName()));
                } else {
                    File newFolder = Paths.get(getPathToSave().toString(), filePath).toFile();
                    if (!newFolder.exists()) {
                        newFolder.mkdirs();
                    }

                    Files.write(Paths.get(getPathToSave().toString(), filePath, fc.getFileName()), fc.getFileContent());
                    //System.out.println(Paths.get(getPathToSave().toString(), filePath, fc.getFileName()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void sendFile(FileInfo fileInfo) {

        if (!fileInfo.isFolder()) {
            FileContent fc = new FileContent(fileInfo.getFileName(), (int) fileInfo.getFileSize());
            try {
                fc.setFileContent(Files.readAllBytes(fileInfo.getFilePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendObject(fc);
        } else if (fileInfo.isFolder()) {
            String parentPath = fileInfo.getFilePath().toString().replace(fileInfo.getFileName(), "");
            try {
                Files.walk(Paths.get(fileInfo.getFilePath().toString()))
                        .filter((e) -> !Files.isDirectory(e))
                        .forEach(path -> {

                            String p = path.toString().replace(parentPath.toString(), "").replace(path.getFileName().toString(), "");
                            try {
                                FileContent fc = new FileContent(p, path.getFileName().toString(), (int) Files.size(path));
                                fc.setFileContent(Files.readAllBytes(path));
                                sendObject(fc);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void askUserList() {
        NetworkMessage nm = new NetworkMessage(Commands.GET_USERS_LIST);
        sendObject(nm);
    }

    public void askFriendsList() {
        NetworkMessage nm = new NetworkMessage(Commands.GET_FRIENDS_LIST);
        sendObject(nm);
    }

    public void shareFolderTo(FileData fd, UserInfo ui, Status shareOrNot) {
        NetworkMessage<ShareFolderTo> nm = new NetworkMessage(Commands.SHARE_FOLDER);
        nm.setContent(new ShareFolderTo(fd.getFileName(), ui.getUserId()));
        nm.setStatus(shareOrNot);
        sendObject(nm);
    }

    public void askMySharesList(FileData fd) {
        if (fd.isFolder()) {
            NetworkMessage nm = new NetworkMessage(Commands.GET_MY_USERSHARES_LIST);
            nm.setExtraInfo(fd.getFileName());
            sendObject(nm);
        }

    }

    public void askFilesList(UserInfo ui) {
        if (ui == null) {
            sendObject(new NetworkMessage(Commands.GET_FILE_LIST));
        } else if(ui.isThisUserIsCurrent()) {
            sendObject(new NetworkMessage(Commands.GET_FILE_LIST));
        }

    }

    public void getSharedFoldersListFromUser(UserInfo ui) {
        if(!ui.isThisUserIsCurrent())
        {
            NetworkMessage<UserInfo> nm = new NetworkMessage(Commands.GO_INTO_SHARED_MODE);
            nm.setContent(ui);
            sendObject(nm);
        }

    }
}



