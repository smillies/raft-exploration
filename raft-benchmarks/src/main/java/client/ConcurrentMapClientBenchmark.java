package client;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
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

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * How to run this benchmark:
 * <ol>
 * <li>mvn clean install
 * <li>start the server cluster (e. g. from within IDE)
 * <li>java -jar target/benchmarks.jar or call main()
 * </ol>
 * Running benchmarks from within an existing project, or even from within the IDE, makes the results less reliable.
 *
 * @author Initial author: Sebastian Millies
 */
@BenchmarkMode({ Mode.Throughput })
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1) // set to zero for local debugging, otherwise no JDWP agent
public abstract class ConcurrentMapClientBenchmark {

  static final int MAX_CLIENTS = 1;
  private static final int MAX_KEY_SPACE = 100000;

  private final Random random = new Random();
  private final ConcurrentMap<Long, String>[] clients = new ConcurrentMap[MAX_CLIENTS];
  private final long[] keySpace = new long[MAX_KEY_SPACE];

  @Setup(Level.Trial)
  public void createClient() {
    System.out.println("Creating clients");
    createSpecificClient(clients);
    for (int i = 0; i < MAX_KEY_SPACE; i++) {
      keySpace[i] = random.nextLong();
      if (i % 1000 == 0) {
        clients[0].put(keySpace[i], "initial-val" + keySpace[i]);
      }
    }
    System.out.println("Clients created");
  }

  abstract void createSpecificClient(ConcurrentMap<Long, String>[] clients);

  @Setup(Level.Iteration)
  public void clearMap() {
    for (int i = 0; i < MAX_CLIENTS; i++) {
      clients[i].clear();
    }
  }

  @Benchmark
  @Group("writer_only")
  @GroupThreads(100)
  public Object put() {
    int j = random.nextInt(MAX_KEY_SPACE);
    int i = j % MAX_CLIENTS;
    return clients[i].put(keySpace[j], "val" + i);
  }

  @Benchmark
  @Group("reader_writer")
  @GroupThreads(20)
  public Object put1() {
    int j = random.nextInt(MAX_KEY_SPACE);
    int i = j % MAX_CLIENTS;

    return clients[i].put(keySpace[j], "val" + i);
  }

  @Benchmark
  @Group("reader_writer")
  @GroupThreads(80)
  public Object get1() {
    int i = random.nextInt(MAX_CLIENTS);
    long key = keySpace[random.nextInt(MAX_KEY_SPACE)];
    return clients[i].get(key);
  }

  public static void main(String[] args) throws RunnerException {
    Locale.setDefault(Locale.ENGLISH);
    Options opt = new OptionsBuilder().verbosity(VerboseMode.EXTRA)
        .include(".*" + ConcurrentMapClientBenchmark.class.getSimpleName() + ".*").build();

    new Runner(opt).run();
  }
}
