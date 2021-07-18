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

import java.util.concurrent.Callable;

@Slf4j
public class Client {


    public SocketChannel mysocketChannel;
    private EventLoopGroup group;

    protected SocketChannel getMysocketChannel() {
        return mysocketChannel;
    }

    public Client() {

        Thread thread = new Thread(() -> {
            group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                new Bootstrap().group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel socketChannel) {
                                mysocketChannel = socketChannel;
                                ChannelPipeline channelPipeline = socketChannel.pipeline();
                                channelPipeline.addLast(new ObjectEncoder());
                                channelPipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                channelPipeline.addLast(new InboundHandlerClient());

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



