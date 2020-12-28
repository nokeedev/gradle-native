package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;

public interface LanguageSourceSetEmptyTester<T extends LanguageSourceSet> {
	T createSubject();

	@Test
	default void hasEmptyFileTreeWhenNoSource() {
		assertThat("file tree should be empty", createSubject().getAsFileTree(), emptyIterable());
	}

	@Test
	default void hasEmptySourceDirectoriesWhenNoSource() {
		assertThat("source directories should be empty", createSubject().getSourceDirectories(), emptyIterable());
	}

	@Test
	default void hasNoFilterPatternByDefault() {
		assertThat(createSubject().getFilter().getExcludes(), empty());
		assertThat(createSubject().getFilter().getIncludes(), empty());
	}
}
