package com.wayeal.cloud.config;

import com.wayeal.cloud.rabbit.RabbitMqInterface;
import com.wayeal.cloud.rabbit.RabbitMqSendUtil;
import com.wayeal.cloud.server.ServerBootStrap;
import com.wayeal.cloud.server.TcpServerChannelInitializer;
import com.wayeal.cloud.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author jian
 * @version 2023-02-16 9:57
 */
@Component
public class ServerBootStrapRunner implements ApplicationRunner {

    private  Thread tcpServerThread;
    @Autowired
    private RabbitMqInterface rabbitMqSendUtil;
    @Autowired
    private CustomDataConfig customDataConfig;
    @Autowired
    private  SessionManager sessionManager;
    @Autowired
    private ProtocolConfig protocolConfig;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        TcpServerChannelInitializer tcpServerChannelInitializer=new TcpServerChannelInitializer(sessionManager,rabbitMqSendUtil,protocolConfig);
        tcpServerThread=new Thread(()->{
            ServerBootStrap server=new ServerBootStrap(tcpServerChannelInitializer,customDataConfig.getPort());
            server.init();
        });
        tcpServerThread.start();
    }
}
