package statemachine;

import io.atomix.copycat.Command;

/*
 * A Command is a state-changing operation.
 */

@SuppressWarnings("serial")
public class PutCommand implements Command<Object> {

	private final Object key;
	private final Object value;

	public PutCommand(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public Object key() {
		return key;
	}

	public Object value() {
		return value;
	}

}