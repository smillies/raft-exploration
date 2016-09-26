package client;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import statemachine.GetQuery;
import statemachine.PutCommand;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConcurrentMapClientTest {

	private static ConcurrentMap<String,String> client;

	@BeforeClass
	public static void connect() {
		CopycatClient copycatClient = CopycatClient.builder()
				// .builder(new Address("localhost", 5001), new Address("localhost", 5002))
				.withTransport(NettyTransport.builder().withThreads(2).build()).build();

		copycatClient.serializer().register(PutCommand.class);
		copycatClient.serializer().register(GetQuery.class);

		CompletableFuture<CopycatClient> future = copycatClient.connect(asList(new Address("localhost", 5001), new Address("localhost", 5099)));
		future.join(); // block
		
		client = new ConcurrentMapClient<>(copycatClient);
	}
	
	@Before
	public void setup() {
		System.out.println("Clearing");
		client.clear();
	}
	
	@Test
	public void entrySet() throws Throwable {
		Map<String, String> expected = new HashMap<>();
		expected.put("baz", "Hello world!");
		
		client.put("baz", "Hello world!");
		try {
			assertEquals(expected.entrySet(), client.entrySet());
		}
		catch (CompletionException e) {
			throw e.getCause();
		}
	}

	@Test
	public void get() throws Throwable {
		client.put("baz", "Hello world!");
		try {
			assertEquals("Hello world!", client.get("baz"));
		}
		catch (CompletionException e) {
			throw e.getCause();
		}
	}
	
	@Test
	public void put() {
		assertNull(client.put("baz", "Hello world!"));
		assertEquals("Hello world!", client.put("baz", "Hello world!"));
	}

	@Test
	public void size() throws Throwable {
		try {
			assertTrue(0 == client.size());
		}
		catch (CompletionException e) {
			throw e.getCause();
		}
	}
}
