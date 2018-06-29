package com.nettyrpc.client;

import com.nettyrpc.client.proxy.IAsyncObjectProxy;
import com.nettyrpc.client.proxy.ObjectProxy;
import com.nettyrpc.registry.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RPC Client（Create RPC proxy）
 *
 * @author luxiaoxun
 */
public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    public RpcClient(String serverAddress) {
        this.serverAddress = serverAddress;
        System.out.println("RpcClient构造函数进来"+this.serverAddress);
        //本地学习增加方法，没有zookeeper支持
        connectServer(this.serverAddress);
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        //动态代理
        T T = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new ObjectProxy<T>(interfaceClass));
        return T;
    }

    public static <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        //本地调试没有zookeeper支持，暂时注掉
        //serviceDiscovery.stop();
        ConnectManage.getInstance().stop();
    }
    //连接服务端,本地方法
    private void connectServer(final String serverAddress) {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());
                String ss[]  = serverAddress.split(":");
                String ip = ss[0];
                int port = Integer.parseInt(ss[1]);
                ChannelFuture channelFuture = b.connect(ip,port);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            logger.info("Successfully connect to remote server. remote peer = " + serverAddress);
                            RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            ConnectManage.getInstance().addHandler(handler);
                        }
                    }
                });
            }
        });
    }
}

