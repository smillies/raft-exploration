package statemachine;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import java.util.HashMap;
import java.util.Map;

public class MapStateMachine<K,V> extends StateMachine implements Snapshottable {

	// this is not a concurrent map. Copycat should serialize all operations on this state machine, so there is no potential for
	// concurrency. Or is there? It would be surprising, given that we  Copycat works by log replication, and the log probably
	// cannot express parallelism
	private Map<K, V> map = new HashMap<K, V>();

	/*
	 * State machine operations are implemented as public methods on the state machine class which accept a single Commit parameter where
	 * the generic argument for the commit is the operation accepted by the method. Copycat automatically detects the command or query that
	 * applies to a given state machine methods based on the generic argument to the Commit parameter.
	 */

	public void clear(Commit<ClearCommand> commit) {
		try {
			map.clear();
		}
		finally {
			commit.close(); // finally block required, not AutoCloseable
		}
	}
	
	public V put(Commit<PutCommand<K,V>> commit) {
		try {
			return map.put(commit.operation().key(), commit.operation().value());
		}
		finally {
			commit.close(); // finally block required, not AutoCloseable
		}
	}

	public V get(Commit<GetQuery<V>> commit) {
		try {
			return map.get(commit.operation().key());
		}
		finally {
			commit.close();
		}
	}
	
	public int size(Commit<SizeQuery> commit) {
		try {
			return map.size();
		}
		finally {
			commit.close();
		}
	}
	
	public Map<K, V> snapshot(Commit<SnapshotQuery<K,V>> commit) {
		try {
			return map;
		}
		finally {
			commit.close();
		}
	}

	/*
	 * For snapshottable state machines, Copycat will periodically request a binary snapshot of the state machine’s state and write the
	 * snapshot to disk. If the server is restarted, the state machine’s state will be recovered from the on-disk snapshot. When a new
	 * server joins the cluster, the snapshot of the state machine will be replicated to the joining server to catch up its state. This
	 * allows Copycat to remove commits that contributed to the snapshot from the replicated log, thus conserving disk space
	 */
	@Override
	public void snapshot(SnapshotWriter writer) {
		writer.writeObject(map);
	}

	@Override
	public void install(SnapshotReader reader) {
		map = reader.readObject();
	}
}
