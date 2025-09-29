package com.wayeal.cloud.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerBootStrap {

    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ServerBootstrap serverBootstrap;

    private Channel serverChannel;

    private TcpServerChannelInitializer serverChannelInitializer;

    public ServerBootStrap (TcpServerChannelInitializer tcpServerChannelInitializer,int port){
           this.serverChannelInitializer=tcpServerChannelInitializer;
           this.port=port;
    }

    public void init()  {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                           .channel(NioServerSocketChannel.class)
                           .childHandler(serverChannelInitializer)
                           .option(ChannelOption.SO_BACKLOG, 128)
                           .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定和接受请求
            Channel serverChannel = serverBootstrap.bind(port).sync().channel();
            // 关闭
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e){
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
