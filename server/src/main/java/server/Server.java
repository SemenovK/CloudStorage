package server;


import constants.Commands;
import constants.Status;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class Server {
    public static Scanner scanner = new Scanner(System.in);
    private DBService dbService;
    private Map<UUID, FileNavigator> usersPlacement;
    private List<FileToWrite> listOfProcessingFiles;
    private SocketChannel serverSocketChannel;

    private EventLoopGroup auth;
    private EventLoopGroup worker;
    private ServerConnectionHandler serverConnectionHandler;

    public Server() {
        usersPlacement = new HashMap<>();
        serverConnectionHandler = new ServerConnectionHandler(this);
        listOfProcessingFiles = new LinkedList<>();
        Thread t = new Thread(() -> {
            log.info("File Conflict Monitor started...");
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
                serverConnectionHandler.setDBService(dbService);
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
                    .filter((e)->!Files.isDirectory(e))
                    .forEach(path -> sendFile(path,fn));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void sendFile(Path filePath, FileNavigator fn){
        String sPath = fn.getCurrentFolder().toString();
        String sFilePath = filePath.toString();
        sFilePath = sFilePath.replace(sPath,"").replace(filePath.getFileName().toString(),"");

        if (filePath.toFile().exists()) {
            try {
                FileContent fileContent = new FileContent(sFilePath,filePath.getFileName().toString(), (int) Files.size(filePath));
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
}
