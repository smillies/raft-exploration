package statemachine;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import java.util.HashMap;
import java.util.Map;

public class MapStateMachine extends StateMachine implements Snapshottable {

	private Map<Object, Object> map = new HashMap<>();

	/*
	 * State machine operations are implemented as public methods on the state machine class which accept a single Commit parameter where
	 * the generic argument for the commit is the operation accepted by the method. Copycat automatically detects the command or query that
	 * applies to a given state machine methods based on the generic argument to the Commit parameter.
	 */

	public Object put(Commit<PutCommand> commit) {
		try {
			return map.put(commit.operation().key(), commit.operation().value());
		}
		finally {
			commit.close(); // finally block required, not AutoCloseable
		}
	}

	public Object get(Commit<GetQuery> commit) {
		try {
			return map.get(commit.operation().key());
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
