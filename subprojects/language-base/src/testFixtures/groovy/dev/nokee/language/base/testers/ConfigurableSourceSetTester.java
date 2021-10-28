package dev.nokee.language.base.testers;

import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import org.gradle.api.tasks.util.PatternFilterable;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface ConfigurableSourceSetTester extends SourceSetTester {
	ConfigurableSourceSet subject();

	@Test
	default void hasFilter() {
		assertThat(subject().getFilter(), isA(PatternFilterable.class));
	}

	@Test
	default void canConfigureFilterUsingAction() {
		ActionTestUtils.MockAction<PatternFilterable> action = ActionTestUtils.mockAction();
		subject().filter(action);
		assertThat(action, calledOnceWith(singleArgumentOf(subject().getFilter())));
	}

	@Test
	default void canConfigureFilterUsingClosure() {
		ClosureTestUtils.MockClosure<Void, PatternFilterable> closure = ClosureTestUtils.mockClosure(PatternFilterable.class);
		subject().filter(closure);
		assertThat(closure, calledOnceWith(singleArgumentOf(subject().getFilter())));
		assertThat(closure, calledOnceWith(allOf(delegateOf(subject().getFilter()), delegateFirstStrategy())));
	}

	@Test
	default void canAddPathsToSourceSet() {
		assertDoesNotThrow(() -> subject().from("some/path"));
	}

	@Test
	default void returnSourceSetWhenAddingPaths() {
		assertThat(subject().from("some/path"), is(subject()));
	}

	@Test
	default void canAddConventionToSourceSet() {
		assertDoesNotThrow(() -> subject().convention("some/path"));
	}

	@Test
	default void returnSourceSetWhenAddingConvention() {
		assertThat(subject().convention("some/path"), is(subject()));
	}
}
