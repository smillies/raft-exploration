package statemachine;

import io.atomix.copycat.Command;

/*
 * A Command is a state-changing operation.
 */

public class PutCommand<K,V> implements Command<V> {

	private static final long serialVersionUID = -7079524886814383447L;
	
	private final K key;
	private final V value;

	public PutCommand(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K key() {
		return key;
	}

	public V value() {
		return value;
	}

}