package statemachine;

import io.atomix.copycat.Command;

public class ClearCommand  implements Command<Void> {

	private static final long serialVersionUID = 5396235533495402351L;

	public ClearCommand() {
	}
	
	@Override
	public CompactionMode compaction() {
		return CompactionMode.TOMBSTONE;
	}

}
