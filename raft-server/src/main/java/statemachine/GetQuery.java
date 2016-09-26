package statemachine;

import io.atomix.copycat.Query;

/*
 * A Query is a state-preserving operation.
 */

public class GetQuery<V> implements Query<V> {
	
	private static final long serialVersionUID = 4052028310236517794L;
	
	private final Object key;

	public GetQuery(Object key) {
		this.key = key;
	}

	public Object key() {
		return key;
	}
	
}