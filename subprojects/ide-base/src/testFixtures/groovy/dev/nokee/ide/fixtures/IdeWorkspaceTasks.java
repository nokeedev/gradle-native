package dev.nokee.ide.fixtures;

import java.util.Arrays;
import java.util.List;

public interface IdeWorkspaceTasks extends IdeProjectTasks {
	String getIdeWorkspace();

	default List<String> getAllToIde() {
		return Arrays.asList(getIdeWorkspace(), getIdeLifecycle());
	}
}
