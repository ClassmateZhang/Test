# NettyRpc
An RPC framework based on Netty, ZooKeeper and Spring  
### Features:
* Simple code and framework
* Non-blocking asynchronous call and Synchronous call support
* Long lived persistent connection
* High availability, load balance and failover
* Service Discovery support by ZooKeeper
### Design:
jdk 1.7.80
### How to use
1. Define an interface:

		public interface HelloService { 
			String hello(String name); 
			String hello(Person person);
		}

2. Implement the interface with annotation @RpcService:

		@RpcService(HelloService.class)
		public class HelloServiceImpl implements HelloService {
			public HelloServiceImpl(){}
			
			@Override
			public String hello(String name) {
				return "Hello! " + name;
			}

			@Override
			public String hello(Person person) {
				return "Hello! " + person.getFirstName() + " " + person.getLastName();
			}
		}

3. Run zookeeper

   For example: zookeeper is running on 127.0.0.1:2181

4. Start server:

   Start server with spring: RpcBootstrap

   Start server without spring: RpcBootstrapWithoutSpring

5. Use the client:
 
		ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
		final RpcClient rpcClient = new RpcClient(serviceDiscovery);
		// Sync call
		HelloService helloService = rpcClient.create(HelloService.class);
		String result = helloService.hello("World");
		// Async call
		IAsyncObjectProxy client = rpcClient.createAsync(HelloService.class);
		RPCFuture helloFuture = client.call("hello", "World");
   		String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
