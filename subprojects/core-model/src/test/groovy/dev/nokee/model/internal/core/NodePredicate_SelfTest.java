package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestActions.doSomethingElse;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Subject(NodePredicate.class)
class NodePredicate_SelfTest {
	@Test
	void canCreateSpecMatchingSelf() {
		val spec = self().scope(path("foo"));
		assertThat(spec.getPath(), optionalWithValue(equalTo(path("foo"))));
		assertThat(spec.getParent(), emptyOptional());
		assertThat(spec.getAncestor(), emptyOptional());
		assertFalse(spec.isSatisfiedBy(node("foo.bar")));
		assertTrue(spec.isSatisfiedBy(node("foo")));
		assertFalse(spec.isSatisfiedBy(node("bar")));
	}

	@Test
	void canCreateSpecMatchingSelfAndPredicate() {
		val spec = self(alwaysFalse()).scope(path("bar"));
		assertThat(spec.getPath(), optionalWithValue(equalTo(path("bar"))));
		assertThat(spec.getParent(), emptyOptional());
		assertThat(spec.getAncestor(), emptyOptional());
		assertFalse(spec.isSatisfiedBy(node("bar")));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canEquals() {
		new EqualsTester()
			.addEqualityGroup(self(), self())
			.addEqualityGroup(self(alwaysFalse()), self(alwaysFalse()))
			.addEqualityGroup(self(node -> true))
			.addEqualityGroup(allDirectDescendants())
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canEqualsScopedPredicate() {
		new EqualsTester()
			.addEqualityGroup(self().scope(path("foo")), self().scope(path("foo")))
			.addEqualityGroup(self().scope(path("bar")))
			.addEqualityGroup(self(alwaysFalse()).scope(path("bar")), self(alwaysFalse()).scope(path("bar")))
			.addEqualityGroup(self(alwaysFalse()).scope(path("foo")))
			.addEqualityGroup(self(node -> true).scope(path("foo")))
			.addEqualityGroup(self(node -> true).scope(path("bar")))
			.addEqualityGroup(self(doSomething()), self(doSomething()))
			.addEqualityGroup(self(doSomethingElse()))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(self(), hasToString("NodePredicate.self()"));
		assertThat(self(alwaysFalse()), hasToString("NodePredicate.self(Predicates.alwaysFalse())"));
		assertThat(self(doSomething()), hasToString("NodePredicate.self(ModelTestActions.doSomething())"));
	}

	@Test
	void willCallbackWithNodeMatchingSelf() {
		val action = mock(ModelAction.class);
		val node = node("a.b");
		self(action).scope(path("a.b")).execute(node);
		verify(action).execute(node);
	}

	@Test
	void willNotCallbackWithNodeMatchingSelf() {
		val action = mock(ModelAction.class);
		val node = node("a");
		self(action).scope(path("a.b")).execute(node);
		verify(action, never()).execute(node);
	}
}
