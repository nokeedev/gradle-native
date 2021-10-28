package dev.nokee.language.base.testers;

import dev.nokee.language.base.SourceSet;
import lombok.val;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface SourceSetTester {
	SourceSet subject();

	@Test
	default void canGetSourceSetAsFileTree() {
		val result = assertDoesNotThrow(() -> subject().getAsFileTree());
		assertThat(result, isA(FileTree.class));
	}

	@Test
	default void canGetSourceSetSourceDirectories() {
		val result = assertDoesNotThrow(() -> subject().getSourceDirectories());
		assertThat(result, isA(FileCollection.class));
	}
}
