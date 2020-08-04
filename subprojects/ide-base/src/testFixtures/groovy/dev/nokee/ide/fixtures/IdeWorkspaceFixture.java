package dev.nokee.ide.fixtures;

import java.util.Arrays;

public interface IdeWorkspaceFixture {
	default IdeWorkspaceFixture assertHasProjects(String... paths) {
		return assertHasProjects(Arrays.asList(paths));
	}

	IdeWorkspaceFixture assertHasProjects(Iterable<String> files);
}
