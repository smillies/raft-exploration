package statemachine;

import io.atomix.copycat.Query;

/*
 * A Query is a state-preserving operation.
 */

@SuppressWarnings("serial")
public class GetQuery implements Query<Object> {
	
	private final Object key;

	public GetQuery(Object key) {
		this.key = key;
	}

	public Object key() {
		return key;
	}
	
}