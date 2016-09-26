package statemachine;

import io.atomix.copycat.Query;
import java.util.Map;


public class SnapshotQuery<K,V> implements Query<Map<K, V>> {
	
	private static final long serialVersionUID = 9198823382676763486L;

	public SnapshotQuery() {
	}
	
}