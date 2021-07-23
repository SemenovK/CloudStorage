package server;


import constants.Commands;
import constants.Status;
import filesystem.FileInfo;
import filesystem.FileNavigator;
import filesystem.FileToWrite;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import network.FileContent;
import network.NetworkAnswer;
import network.NetworkMessage;
import network.UserData;
import objects.FileData;
import objects.ShareFolderTo;
import objects.UserInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class Server {
    public static Scanner scanner = new Scanner(System.in);
    private DBService dbService;
    private Map<UUID, FileNavigator> usersPlacement;
    private List<FileToWrite> listOfProcessingFiles;


    private EventLoopGroup auth;
    private EventLoopGroup worker;
    private ServerConnectionHandler serverConnectionHandler;
    private SocketChannel serverSocketChannel;

    public Server() {
        usersPlacement = new HashMap<>();
        serverConnectionHandler = new ServerConnectionHandler(this);
        listOfProcessingFiles = new LinkedList<>();

        Thread t = new Thread(() -> {
            log.info("FileConflict Monitor started...");
            while (true) {
                try {

                    if (!FileNavigator.getFilesAwait().isEmpty()) {
                        FileToWrite ftw = FileNavigator.getFilesAwait().peek();
                        NetworkAnswer answer = new NetworkAnswer();
                        answer.setQuestionMessageType(Commands.FILE_DATA);
                        answer.setAnswer(Status.FILE_EXISTS);
                        answer.setExtraInfo(ftw.getFileName());
                        answer.setUid(ftw.getUuid());
                        serverSocketChannel.writeAndFlush(answer);
                        listOfProcessingFiles.add(FileNavigator.getFilesAwait().removeFirst());
                    }

                } catch (Exception e) {
                    log.error(e.getMessage());
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }

        });
        t.setDaemon(true);
        t.start();

    }

    public Map<UUID, FileNavigator> getUsersPlacement() {
        return usersPlacement;
    }

    public static void main(String[] args) {
        String commandString;
        Server server = new Server();

        server.start();
        while (true) {
            commandString = scanner.nextLine();

            if (commandString.equals("/quit")) {
                server.stop();
                break;
            }
            if (commandString.startsWith("/userAdd")) {
                //todo
            }
        }
        scanner.close();
    }


    public void start() {
        Thread t = new Thread(() -> {
            auth = new NioEventLoopGroup(1);
            worker = new NioEventLoopGroup();
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(auth, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                serverSocketChannel = socketChannel;
                                ChannelPipeline channelPipeline = socketChannel.pipeline();
                                channelPipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                channelPipeline.addLast(new ObjectEncoder());
                                channelPipeline.addLast(serverConnectionHandler);

                            }
                        });
                ChannelFuture channelFuture = serverBootstrap.bind(20115).sync();
                log.info("Server started");
                dbService = new DBService();
                dbService.start();
                log.info("Connected to database");


                channelFuture.channel().closeFuture().sync();
            } catch (Exception E) {
                log.error(E.getMessage());
            } finally {
                auth.shutdownGracefully();
                worker.shutdownGracefully();
            }

        });
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }

    public synchronized void fileWritingDecision(String fileName, UUID uuid, Status status) {

        Iterator<FileToWrite> iterator = listOfProcessingFiles.iterator();
        while (iterator.hasNext()) {
            FileToWrite f = iterator.next();
            if (f.getFileName().equals(fileName) && f.getUuid().equals(uuid)) {
                if (status.equals(Status.OK)) {
                    iterator.remove();
                    f.setDoWrite(true);
                    usersPlacement.get(uuid).putFileToQueue(f);
                } else if (status.equals(Status.CANCEL)) {
                    iterator.remove();
                }
            }
        }
    }

    public synchronized void sendFile(String fileName, UUID uuid) {
        FileNavigator fn = getUsersPlacement().get(uuid);
        Path p = Paths.get(fn.getCurrentFolder().toString(), fileName);
        try {
            Files.walk(Paths.get(fn.getCurrentFolder().toString()))
                    .filter((e) -> !Files.isDirectory(e))
                    .forEach(path -> sendFile(path, fn));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void sendFile(Path filePath, FileNavigator fn) {
        String sPath = fn.getCurrentFolder().toString();
        String sFilePath = filePath.toString();
        sFilePath = sFilePath.replace(sPath, "").replace(filePath.getFileName().toString(), "");

        if (filePath.toFile().exists()) {
            try {
                FileContent fileContent = new FileContent(sFilePath, filePath.getFileName().toString(), (int) Files.size(filePath));
                fileContent.setFileContent(Files.readAllBytes(filePath));
                NetworkAnswer<FileContent> answer = new NetworkAnswer<>();
                answer.setAnswer(fileContent);
                answer.setQuestionMessageType(Commands.FILE_DOWNLOAD);
                answer.setUid(fn.getUuid());
                serverSocketChannel.writeAndFlush(answer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public void createUsersList(UUID uuid) {
        FileNavigator fn = getUsersPlacement().get(uuid);
        ResultSet result = dbService.getUsersList();
        NetworkAnswer<UserInfo> answer = new NetworkAnswer<>();
        int i = 0;
        answer.setQuestionMessageType(Commands.GET_USERS_LIST);
        answer.setUid(fn.getUuid());
        try {
            while (result.next()) {
                answer.setCurrentPart(i++);
                int userId = result.getInt(1);
                String username = result.getString(2);
                answer.setAnswer(new UserInfo(userId, username, userId == fn.getUserid()));
                serverSocketChannel.writeAndFlush(answer);
            }
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
        }

    }

    public void createFriendsList(UUID uuid) {
        FileNavigator fn = getUsersPlacement().get(uuid);
        ResultSet result = dbService.getFriendsList(fn.getUserid());
        NetworkAnswer<UserInfo> answer = new NetworkAnswer<>();
        int i = 0;
        answer.setQuestionMessageType(Commands.GET_FRIENDS_LIST);
        answer.setUid(fn.getUuid());
        try {
            while (result.next()) {
                answer.setCurrentPart(i++);
                int userId = result.getInt(1);
                String username = result.getString(2);
                answer.setAnswer(new UserInfo(userId, username, userId == fn.getUserid()));
                serverSocketChannel.writeAndFlush(answer);
                System.out.println(answer);
            }
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
        }
    }

    public void authUser(NetworkMessage nm) {
        NetworkAnswer answer = new NetworkAnswer();
        answer.setQuestionMessageType(nm.getMessagePurpose());

        UserData ud = (UserData) nm;
        boolean auth = dbService.authUser(ud);
        answer.setStatus(auth ? Status.OK : Status.DENIED);
        if (usersPlacement != null && auth) {
            UUID uid = UUID.randomUUID();
            usersPlacement.put(uid, new FileNavigator(uid, ud.getUserID()));
            answer.setUid(uid);
            answer.setAnswer(Status.OK);

        } else if (usersPlacement != null && !auth) {
            answer.setAnswer(Status.DENIED);
        }
        serverSocketChannel.writeAndFlush(answer);
    }

    public void createFileList(NetworkMessage nm) {

        FileNavigator fn = usersPlacement.get(nm.getUid());
        try {
            String s = nm.getExtraInfo();
            if (s != null) {
                if (!s.equals("") && !s.equals("..")) {
                    fn.goInto(s);
                } else if (s.equals("..")) {
                    if (fn.isSharedMode() && fn.isOnTop()) {
                        NetworkMessage<UserInfo> tmp = new NetworkMessage(Commands.GO_INTO_SHARED_MODE);
                        tmp.setUid(nm.getUid());
                        tmp.setContent(new UserInfo(fn.getSharedUserId(), "", false));
                        nm = tmp;
                        goInShareMode(nm);
                        return;
                    } else {
                        fn.goUp();
                    }


                }
            }

            List<FileInfo> fileInfo = fn.getFilesListFromCurrent();
            NetworkAnswer na = new NetworkAnswer<FileData>();
            na.setUid(nm.getUid());
            int partnum = 0;

            if(!fn.isSharedMode()){
                if (!fn.isOnTop()) {
                    na.setQuestionMessageType(nm.getMessagePurpose());
                    na.setAnswer(new FileData("..", -2, true));
                    serverSocketChannel.writeAndFlush(na);
                }

                partnum = (fn.isOnTop() ? 0 : 1);
            } else {
                na.setQuestionMessageType(nm.getMessagePurpose());
                na.setAnswer(new FileData("..", -2, true));
                serverSocketChannel.writeAndFlush(na);
                partnum = 1;
            }



            for (FileInfo f : fileInfo) {
                na.setQuestionMessageType(nm.getMessagePurpose());
                FileData fd = new FileData(f.getFileName(), f.getFileSize(), f.isFolder());
                if (f.isFolder()) {
                    fd.setSharedFolder(dbService.isFolderShared(fn.getUserid(), f.getFileName()));
                }

                na.setAnswer(fd);
                na.setCurrentPart(partnum++);
                serverSocketChannel.writeAndFlush(na);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shareFolderManage(NetworkMessage nm) {
        FileNavigator fn = usersPlacement.get(nm.getUid());
        ShareFolderTo sft = (ShareFolderTo) nm.getContent();
        Status status = nm.getStatus();
        if (status.equals(Status.OK)) {
            dbService.addSharedFolder(fn.getUserid(), sft.getToUserId(), sft.getFolderName(), fn.getCurrentFolder().toString());
        } else if (status.equals(Status.CANCEL)) {
            dbService.removeSharedFolder(fn.getUserid(), sft.getToUserId(), sft.getFolderName());

        }

    }

    public void collectUserShares(NetworkMessage nm) {
        FileNavigator fn = usersPlacement.get(nm.getUid());
        NetworkAnswer<UserInfo> answer = new NetworkAnswer<>();
        ResultSet result;
        result = dbService.getMyFriends(fn);
        int i = 0;
        answer.setQuestionMessageType(Commands.GET_MY_USERSHARES_LIST);
        answer.setUid(fn.getUuid());
        try {
            while (result.next()) {
                answer.setCurrentPart(i++);
                int userId = result.getInt(1);
                String username = result.getString(2);
                answer.setAnswer(new UserInfo(userId, username, userId == fn.getUserid()));
                serverSocketChannel.writeAndFlush(answer);
                System.out.println(answer);
            }
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
        }
    }

    public void goInShareMode(NetworkMessage nm) {
        FileNavigator fn = usersPlacement.get(nm.getUid());
        UserInfo ui = (UserInfo) nm.getContent();
        System.out.println(ui);
        NetworkAnswer<FileData> answer = new NetworkAnswer<>();
        ResultSet result;
        result = dbService.getUserSharedFolders(ui.getUserId(), fn.getUserid());
        fn.setSharedUserId(ui.getUserId());
        int i = 0;
        answer.setQuestionMessageType(Commands.GO_INTO_SHARED_MODE);
        answer.setUid(fn.getUuid());
        try {
            while (result.next()) {
                answer.setCurrentPart(i++);
                String folderName = result.getString(1);
                int folderID = result.getInt(2);
                answer.setAnswer(new FileData(folderName, folderID));
                serverSocketChannel.writeAndFlush(answer);
                System.out.println(answer);
            }
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
        }

    }

    public void goToNormal(NetworkMessage nm) {
        FileNavigator fn = usersPlacement.get(nm.getUid());
        usersPlacement.put(nm.getUid(), new FileNavigator(nm.getUid(), fn.getUserid()));
    }

    public void getSharedFileList(NetworkMessage nm) {
        FileNavigator fn = usersPlacement.get(nm.getUid());
        int folderId = Integer.parseInt(nm.getExtraInfo());
        ResultSet result;
        result = dbService.getFolderPath(folderId);
        String folderPath = "";
        String folderName = "";

        try {
            while (result.next()) {
                folderPath = result.getString(1);
                folderName = result.getString(2);
            }
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
        }
        FileNavigator tmp = new FileNavigator(nm.getUid(), fn.getUserid(), folderPath, folderName);
        tmp.setSharedUserId(fn.getSharedUserId());
        usersPlacement.put(nm.getUid(), tmp);
        createSharedFileList(nm);
    }

    private void createSharedFileList(NetworkMessage nm) {
        FileNavigator fn = usersPlacement.get(nm.getUid());
        try {

            List<FileInfo> fileInfo = fn.getFilesListFromCurrent();
            int totalParts = fileInfo.size() + (fn.isOnTop() ? 0 : 1);
            NetworkAnswer na = new NetworkAnswer<FileData>();
            na.setUid(nm.getUid());

            na.setQuestionMessageType(nm.getMessagePurpose());
            na.setAnswer(new FileData("..", -2, true));
            serverSocketChannel.writeAndFlush(na);

            int partnum = 1;

            for (FileInfo f : fileInfo) {
                na.setQuestionMessageType(nm.getMessagePurpose());
                FileData fd = new FileData(f.getFileName(), f.getFileSize(), f.isFolder());

                na.setAnswer(fd);
                na.setCurrentPart(partnum++);
                serverSocketChannel.writeAndFlush(na);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
