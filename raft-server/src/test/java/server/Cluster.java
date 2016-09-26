package server;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import statemachine.GetQuery;
import statemachine.MapStateMachine;
import statemachine.PutCommand;

import static java.util.Collections.singleton;

/**
 * Copycat uses the builder pattern for configuring and constructing servers.
 * <ul>
 * <li>Each Copycat server must be initialized with a local server Address.
 * <li>Each server must be configured with the same state machine.
 * <li>The server must be provided a Transport through which it will communicate with other servers in the cluster.
 * <li>The server must have a Storage object through which it will store the cluster configuration and state changes. The server’s Storage
 * directory must be <em>unique</em> to the server.
 * </ul>
 * <p>
 * One final task is necessary to complete the configuration of the server. We’ve created two state machine operations - PutCommand and
 * GetQuery - which are Serializable. By default, Copycat’s serialization framework will serialize these operations using Java’s
 * serialization. However, users can explicitly register serializable classes and implement custom binary serializers for more efficient
 * serialization.
 * <p>
 * Once the server has been built, we can bootstrap a new cluster by calling the bootstrap() method. When a server is bootstrapped, it forms
 * a new cluster single node cluster to which additional servers can be joined via the join() method. When joining an existing cluster, the
 * existing cluster configuration must be provided to the join method.
 * @author Initial author: Sebastian Millies
 */

public class Cluster {

	public static CopycatServer createServer(String host, int port) {
		Address address = new Address(host, port);
		CopycatServer server = CopycatServer.builder(address).withStateMachine(MapStateMachine::new)
//				.withTransport(NettyTransport.builder().withThreads(4).build()) // Netty is default
				.withStorage(
						Storage.builder().withDirectory(new File(host + "_" + port + "_logs")).withStorageLevel(StorageLevel.DISK).build())
				.build();
		
		server.serializer().register(PutCommand.class);
		server.serializer().register(GetQuery.class);
		return server;
	}
	
	public static void main(String[] args) {
		CopycatServer server1 = createServer("localhost", 5001);
		CopycatServer server2 = createServer("localhost", 5002);
		CopycatServer server3 = createServer("localhost", 5003);
		
		CompletableFuture<Void> future1 = server1.bootstrap().thenRun(() -> System.out.println("Server1 bootstrapped"));
		CompletableFuture<Void> future2 = server2.join(singleton(new Address("localhost", 5001))).thenRun(() -> System.out.println("Server2 joined cluster"));
		CompletableFuture<Void> future3 = server3.join(singleton(new Address("localhost", 5001))).thenRun(() -> System.out.println("Server3 joined cluster"));
		CompletableFuture.allOf(future1, future2, future3).thenRun(() -> System.out.println("Cluster is ready"));
	}
}
