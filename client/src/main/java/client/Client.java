package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import lombok.extern.slf4j.Slf4j;
import objects.NetworkMessage;

@Slf4j
public class Client {


    public SocketChannel mysocketChannel;
    private EventLoopGroup group;
    ClientConnectionHandler handler;
    protected SocketChannel getMysocketChannel() {
        return mysocketChannel;
    }

    public ClientConnectionHandler getHandler() {
        return handler;
    }

    public Client() {
        handler = new ClientConnectionHandler();
        Thread thread = new Thread(() -> {
            group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                new Bootstrap().group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                super.exceptionCaught(ctx, cause);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(ctx.pipeline());
                                super.channelRead(ctx, msg);
                            }

                            @Override
                            public void initChannel(SocketChannel socketChannel) {
                                mysocketChannel = socketChannel;
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

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        });

        thread.setDaemon(true);
        thread.start();

    }

    public void SendObject(NetworkMessage nm){
        mysocketChannel.writeAndFlush(nm);
    }

    public void close(){
        group.shutdownGracefully();
        mysocketChannel.close();
    }
}



