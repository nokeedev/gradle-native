package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyAll;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyNone;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestActions.doSomethingElse;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(ModelActions.class)
class ModelActions_MatchingTest {
	@Test
	void executesActionIfPredicateIsTrue() {
		val action = mock(ModelAction.class);
		val node = node();
		matching(satisfyAll(), action).execute(node);
		verify(action, times(1)).execute(node);
	}

	@Test
	void doesNotExecuteActionIfPredicateIsFalse() {
		val action = mock(ModelAction.class);
		val node = node();
		matching(satisfyNone(), action).execute(node);
		verify(action, never()).execute(node);
	}

	@Test
	void checkToString() {
		assertThat(matching(satisfyAll(), doSomething()),
			hasToString("ModelActions.matching(ModelSpecs.satisfyAll(), ModelTestActions.doSomething())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(matching(satisfyAll(), doSomething()), matching(satisfyAll(), doSomething()))
			.addEqualityGroup(matching(satisfyNone(), doSomething()))
			.addEqualityGroup(matching(satisfyAll(), doSomethingElse()))
			.testEquals();
	}
}
