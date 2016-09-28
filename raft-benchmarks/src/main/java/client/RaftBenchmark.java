package client;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import statemachine.GetQuery;
import statemachine.PutCommand;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import static java.util.Arrays.asList;

/**
 *
 *
 */
public class RaftBenchmark extends ConcurrentMapClientBenchmark {
  @Override
  void createSpecificClient(ConcurrentMap<Long, String>[] clients) {
    for (int i = 0; i < MAX_CLIENTS; i++) {
      CopycatClient copycatClient = CopycatClient.builder()
          .withTransport(NettyTransport.builder().withThreads(2).build()).build();

      copycatClient.serializer().register(PutCommand.class);
      copycatClient.serializer().register(GetQuery.class);

      CompletableFuture<CopycatClient> future = copycatClient
          .connect(asList(new Address("localhost", 5001),
              new Address("localhost", 5002),
              new Address("localhost", 5003)));
      future.join(); // block

      clients[i] = new ConcurrentMapClient<>(copycatClient);
    }
  }
}