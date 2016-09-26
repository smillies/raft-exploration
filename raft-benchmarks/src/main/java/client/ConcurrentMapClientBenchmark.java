package client;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;
import statemachine.GetQuery;
import statemachine.PutCommand;

import static java.util.Arrays.asList;

/**
 * How to run this benchmark:
 * <ol>
 * <li>mvn clean install
 * <li>start the server cluster (e. g. from within IDE)
 * <li>java -jar target/benchmarks.jar or call main()
 * </ol>
 * Running benchmarks from within an existing project, or even from within the IDE, makes the results less reliable.
 * @author Initial author: Sebastian Millies
 */
@BenchmarkMode({ Mode.Throughput })
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1) // set to zero for local debugging, otherwise no JDWP agent
public class ConcurrentMapClientBenchmark {

	private Random random;
	private ConcurrentMap<Long, String> client;

	@Setup(Level.Trial)
	public void createClient() {
		random = new Random();

		System.out.println("Creating client");
		CopycatClient copycatClient = CopycatClient.builder()
				// .builder(new Address("localhost", 5001), new Address("localhost", 5002))
				.withTransport(NettyTransport.builder().withThreads(2).build()).build();

		copycatClient.serializer().register(PutCommand.class);
		copycatClient.serializer().register(GetQuery.class);

		CompletableFuture<CopycatClient> future = copycatClient
				.connect(asList(new Address("localhost", 5001), new Address("localhost", 5099)));
		future.join(); // block

		client = new ConcurrentMapClient<>(copycatClient);
	}

	@Setup(Level.Iteration)
	public void clearMap() {
		client.clear();
	}

	@Benchmark
	public Object put() {
		return client.put(random.nextLong(), "val");
	}

	@Benchmark
	public Object putAndGet() {
		long key = random.nextLong();
		client.put(key, "val");
		return client.get(key);
	}

	public static void main(String[] args) throws RunnerException {
		Locale.setDefault(Locale.ENGLISH);
		Options opt = new OptionsBuilder().verbosity(VerboseMode.EXTRA)
				.include(".*" + ConcurrentMapClientBenchmark.class.getSimpleName() + ".*").build();

		new Runner(opt).run();
	}
}
