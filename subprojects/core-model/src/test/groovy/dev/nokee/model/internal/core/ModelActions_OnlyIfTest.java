package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.internal.core.ModelActions.onlyIf;
import static dev.nokee.model.internal.core.ModelActions.doNothing;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(ModelActions.class)
class ModelActions_OnlyIfTest {
	@Test
	void executesActionIfPredicateIsTrue() {
		val action = mock(ModelAction.class);
		val node = node();
		onlyIf(alwaysTrue(), action).execute(node);
		verify(action, times(1)).execute(node);
	}

	@Test
	void doesNotExecuteActionIfPredicateIsFalse() {
		val action = mock(ModelAction.class);
		val node = node();
		onlyIf(alwaysFalse(), action).execute(node);
		verify(action, never()).execute(node);
	}

	@Test
	void checkToString() {
		assertThat(onlyIf(alwaysTrue(), doNothing()),
			hasToString("ModelActions.onlyIf(Predicates.alwaysTrue(), ModelActions.doNothing())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(onlyIf(alwaysTrue(), doNothing()), onlyIf(alwaysTrue(), doNothing()))
			.addEqualityGroup(onlyIf(alwaysFalse(), doNothing()))
			.addEqualityGroup(onlyIf(alwaysTrue(), it -> {}))
			.testEquals();
	}
}
