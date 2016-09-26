package client;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.BeforeClass;
import org.junit.Test;
import statemachine.GetQuery;
import statemachine.PutCommand;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ClientDriver {

	private static CopycatClient client;

	@BeforeClass
	public static void connect() {
		client = CopycatClient.builder()
				// .builder(new Address("localhost", 5001), new Address("localhost", 5002))
				.withTransport(NettyTransport.builder().withThreads(2).build()).build();

		client.serializer().register(PutCommand.class);
		client.serializer().register(GetQuery.class);

		/*
		 * When connecting to a cluster, a collection of server addresses must be passed to the connect method. The address list does not
		 * have to be representative of the entire cluster, but the client must be able to reach at least one server to establish a new
		 * session. Clients’ communication with the cluster is completely transparent to the user. If a client’s connection to a server
		 * fails, the client will automatically attempt to reconnect to remaining servers and keep its session alive. Users can control
		 * client fault-tolerance behavior through various strategies configurable in the client builder.
		 */

		CompletableFuture<CopycatClient> future = client.connect(asList(new Address("localhost", 5001), new Address("localhost", 5099)));
		future.join(); // block
	}

	@Test
	public void runNonBlocking() {
		// Submit two PutCommands to the replicated state machine
		// all Copycat APIs are asynchronous and rely upon Java 8’s CompletableFuture as a promises API. So, instead of blocking on a single
		// operation, a client can submit multiple operations and either await the result or react to the result once it has been received
		CompletableFuture[] futures = new CompletableFuture[2];
		futures[0] = client.submit(new PutCommand("foo", "Hello world!"));
		futures[1] = client.submit(new PutCommand("bar", "Hello world!"));

		// Print a message once all commands have completed
		CompletableFuture.allOf(futures).thenRun(() -> System.out.println("Commands completed!"));
	}

	@Test
	public void query() throws Throwable {
		CompletableFuture<Void> future = client.submit(new PutCommand("baz", "Hello world!"))
				.thenCompose(_v -> client.submit(new GetQuery("baz"))).thenAccept(r -> assertEquals("Hello world!", r));
		try {
			future.join();
		}
		catch (CompletionException e) {
			throw e.getCause();
		}
	}
}
