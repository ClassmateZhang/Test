package com.nettyrpc.test.server;

import com.nettyrpc.registry.ServiceRegistry;
import com.nettyrpc.server.RpcServer;
import com.nettyrpc.test.client.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcBootstrapWithoutSpring {
    private static final Logger logger = LoggerFactory.getLogger(RpcBootstrapWithoutSpring.class);

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:18866";
        //本机运行不需要zookeeper注册（如果不用zookeeper，想其他方法替代注册中心）
        //ServiceRegistry serviceRegistry = new ServiceRegistry("127.0.0.1:2181");
        RpcServer rpcServer = new RpcServer(serverAddress);
        HelloService helloService = new HelloServiceImpl();
        rpcServer.addService("com.nettyrpc.test.client.HelloService", helloService);
        try {
            rpcServer.start();
        } catch (Exception ex) {
            logger.error("Exception: {}", ex);
        }
    }
}
