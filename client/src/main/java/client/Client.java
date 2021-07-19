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
import network.NetworkMessage;

import java.util.UUID;

@Slf4j
public class Client {


    private SocketChannel mySocketChannel;
    private EventLoopGroup group;
    private ClientConnectionHandler handler;
    private UUID userToken;
    private boolean connected;

    public boolean isConnected() {
        return connected;
    }


    public UUID getUserToken() {
        return userToken;
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

    public Client() {
        handler = new ClientConnectionHandler(this);
        connected = false;
        Thread thread = new Thread(() -> {
            group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                new Bootstrap().group(group)
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

    }

    public void SendObject(NetworkMessage nm) {
        nm.setUid(this.userToken);
        System.out.println(this.userToken);
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
}



