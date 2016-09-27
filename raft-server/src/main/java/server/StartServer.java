package server;

import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.ValueConverter;
import statemachine.ClearCommand;
import statemachine.GetQuery;
import statemachine.MapStateMachine;
import statemachine.PutCommand;
import statemachine.SizeQuery;
import statemachine.SnapshotQuery;
import util.FileUtils;

import static java.util.Collections.singleton;
import static server.StartServer.AddressConverter.addressConverter;

/**
 * Starts a {@code CopycatServer}. The main method accepts the following command line options:
 * <ul>
 * <li>-c, --clean (delete the storage directory before starting)
 * <li>-a, --address (host:port)
 * <li>-j, --join (join cluster at address)
 * </ul>
 * If join is not given, the server is bootstrapped.
 * @author Initial author: Sebastian Millies
 */
public class StartServer {

	public static void main(String[] args) throws IOException {
		OptionParser parser = new OptionParser();
		parser.accepts("clean");
		OptionSpec<Address> addressOption = parser.accepts("address").withRequiredArg().required().withValuesConvertedBy(addressConverter());
		OptionSpec<Address> joinOption = parser.accepts("join").withRequiredArg().withValuesConvertedBy(addressConverter());
		OptionSet options = parser.parse(args);
		
		Address address = options.valueOf(addressOption);
		CopycatServer server = createServer(address);
		
		if (options.has("clean")) {
			Path storageDir = Paths.get(storageDir(address));
			FileUtils.deleteDirectory(storageDir);
			System.out.println("Deleted storage at " +  storageDir.toRealPath());
		}
		
		if (options.has("join")) {
			Address cluster = options.valueOf(joinOption);
			server.join(singleton(cluster)).thenRun(() -> System.out.println("Server " + address + " joined cluster at " + cluster));
		}
		else {
			server.bootstrap().thenRun(() -> System.out.println("Server " + address + " bootstrapped"));
		}
	}
	
	private static CopycatServer createServer(Address address) {
		CopycatServer server = CopycatServer.builder(address).withStateMachine(MapStateMachine::new)
//				.withTransport(NettyTransport.builder().withThreads(4).build()) // Netty is default
				.withStorage(
						Storage.builder().withDirectory(new File(storageDir(address))).withStorageLevel(StorageLevel.DISK).build())
				.build();
		
		server.serializer().register(ClearCommand.class);
		server.serializer().register(PutCommand.class);
		server.serializer().register(GetQuery.class);
		server.serializer().register(SizeQuery.class);
		server.serializer().register(SnapshotQuery.class);
		
		return server;
	}

	private static String storageDir(Address address) {
		return address.host() + "_" + address.port() + "_logs";
	}
	
	static class AddressConverter implements ValueConverter<Address> {
		
		public static final AddressConverter addressConverter() {
			return new AddressConverter();
		}
		
		@Override
		public Address convert(String value) {
			String[] part = value.split(":");
			return new Address(part[0], Integer.valueOf(part[1]));
		}

		@Override
		public Class<? extends Address> valueType() {
			return Address.class;
		}

		@Override
		public String valuePattern() {
			return null;
		}
	}

}
