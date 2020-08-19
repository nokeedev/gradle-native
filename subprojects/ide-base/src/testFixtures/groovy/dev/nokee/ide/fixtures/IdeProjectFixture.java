package dev.nokee.ide.fixtures;

import java.util.Arrays;

public interface IdeProjectFixture {
	default IdeProjectFixture assertHasBuildFiles(String... buildFiles) {
		return assertHasBuildFiles(Arrays.asList(buildFiles));
	}

	IdeProjectFixture assertHasBuildFiles(Iterable<String> files);

	default IdeProjectFixture assertHasBuildTypes(String... buildTypes) {
		return assertHasBuildTypes(Arrays.asList(buildTypes));
	}

	IdeProjectFixture assertHasBuildTypes(Iterable<String> buildTypes);
}
