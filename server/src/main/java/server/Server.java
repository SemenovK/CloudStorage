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
import network.NetworkAnswer;

import java.util.*;

@Slf4j
public class Server {
    public static Scanner scanner = new Scanner(System.in);
    private DBAuthService dbAuthService;
    private Map<UUID, FileNavigator> usersPlacement;
    List<FileToWrite> listOfProcessingFiles;
    private SocketChannel serverSocketChannel;

    private EventLoopGroup auth;
    private EventLoopGroup worker;
    private ServerConnectionHandler serverConnectionHandler;

    public Server() {
        usersPlacement = new HashMap<>();
        serverConnectionHandler = new ServerConnectionHandler(this);
        listOfProcessingFiles = new LinkedList<>();
        Thread t = new Thread(() -> {
            log.info("Monitor started...");
            while (true) {
                try {
                    if (FileNavigator.class != null) {
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
                    }

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
        String commandString = "";
        Server server = new Server();

        server.start();
        while (true) {
            commandString = scanner.nextLine();

            if (commandString.equals("/quit")) {
                server.stop();
                break;
            }

            System.out.println("Command:" + commandString);


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
                dbAuthService = new DBAuthService();
                dbAuthService.start();
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

        Iterator<FileToWrite> iter = listOfProcessingFiles.iterator();
        while (iter.hasNext()) {
            FileToWrite f = iter.next();
            if (f.getFileName().equals(fileName) && f.getUuid().equals(uuid)) {
                if (status.equals(Status.OK)) {
                    iter.remove();
                    f.setDoWrite(true);
                    System.out.println("Ready "+f.toString());
                    usersPlacement.get(uuid).putFileToQueue(f);
                } else if (status.equals(Status.CANCEL)) {
                    iter.remove();
                }
            }
        }
    }
}
