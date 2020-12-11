package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(ModelActions.class)
class ModelActions_OnceTest {
	@Test
	void executeDelegateActionOnlyOnce() {
		val node = node();
		val delegate = mock(ModelAction.class);
		val action = once(delegate);
		action.execute(node);
		action.execute(node);

		verify(delegate, times(1)).execute(node);
	}

	@Test
	void checkToString() {
		assertThat(once(ModelActions.doNothing()), hasToString("ModelActions.once(ModelActions.doNothing())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(once(ModelActions.doNothing()), once(ModelActions.doNothing()))
			.addEqualityGroup(once(t -> {}))
			.testEquals();
	}
}
