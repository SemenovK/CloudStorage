package server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Server {
    public static Scanner scanner = new Scanner(System.in);
    private DBAuthService dbAuthService;
    private EventLoopGroup auth;
    private EventLoopGroup worker;

    public Server(){

    }

    public static void main(String[] args) {
        String commandString="";
        Server server = new Server();

        server.start();
        while(true){
            commandString = scanner.nextLine();

            if(commandString.equals("/quit")){
                server.stop();
                break;
            }

            System.out.println("Command:"+commandString);


        }
        scanner.close();
    }


    public void start(){
        Thread t = new Thread(()->{
            auth = new NioEventLoopGroup(1);
            worker = new NioEventLoopGroup();
            try{
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(auth,worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline channelPipeline = socketChannel.pipeline();
                                channelPipeline.addLast(new ObjectEncoder());
                                channelPipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                channelPipeline.addLast(new InboundHandler());

                            }
                        });
                ChannelFuture channelFuture = serverBootstrap.bind(20115).sync();
                log.info("Server started");
                dbAuthService = new DBAuthService();
                dbAuthService.start();
                log.info("Connected to database");

                channelFuture.channel().closeFuture().sync();
            } catch (Exception E){
                log.error(E.getMessage());
            } finally {
                auth.shutdownGracefully();
                worker.shutdownGracefully();
            }

        });
        t.setDaemon(true);
        t.start();
    }

    public void stop(){
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
