package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.TestProjection;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.ConsumerTestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.core.NodePredicates.ofType;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

public interface ModelNodeMatchingTester {
	ModelNode createSubject();

	@Test
	default void canMatchProjectionNodeAndKnownObjectUsingAction() {
		val action = ConsumerTestUtils.mockBiConsumer();
		val subject = createSubject();
		val nodeA = subject.node("a");
		val knownA = nodeA.projection(TestProjection.class);
		subject.all(ofType(TestProjection.class), action);
		assertThat(action, calledOnceWith(allOf(firstArgumentOf(nodeA), secondArgumentOf(knownA))));

		val nodeB = nodeA.node("b");
		val knownAB = nodeB.projection(TestProjection.class);
		assertThat(action, allOf(called(2L), lastArgument(allOf(firstArgumentOf(nodeB), secondArgumentOf(knownAB)))));
	}

	@Test
	default void canMatchProjectionNodeAndKnownObjectUsingActionClass() {
		val action = TestAction.mock = ConsumerTestUtils.mockBiConsumer();
		val subject = createSubject();
		val nodeA = subject.node("a");
		val knownA = nodeA.projection(TestProjection.class);
		subject.all(ofType(TestProjection.class), TestAction.class);
		assertThat(action, calledOnceWith(allOf(firstArgumentOf(nodeA), secondArgumentOf(knownA))));

		val nodeB = nodeA.node("b");
		val knownAB = nodeB.projection(TestProjection.class);
		assertThat(action, allOf(called(2L), lastArgument(allOf(firstArgumentOf(nodeB), secondArgumentOf(knownAB)))));
	}

	// WARNING: The action class is not reentrant and should not use multiple instance
	class TestAction implements BiConsumer<ModelNode, KnownDomainObject<TestProjection>> {
		static ConsumerTestUtils.MockBiConsumer<ModelNode, KnownDomainObject<TestProjection>> mock;

		@Override
		public void accept(ModelNode node, KnownDomainObject<TestProjection> knownObject) {
			mock.accept(node, knownObject);
		}
	}

	@Test
	default void canMatchProjectionNodeAndKnownObjectUsingClosure() {
		val action = ClosureTestUtils.mockClosure(KnownDomainObject.class);
		val subject = createSubject();
		val nodeA = subject.node("a");
		val knownA = nodeA.projection(TestProjection.class);
		subject.all(ofType(TestProjection.class), action);
		assertThat(action, calledOnceWith(allOf(delegateFirstStrategy(), delegateOf(nodeA), singleArgumentOf(knownA))));

		val nodeB = nodeA.node("b");
		val knownAB = nodeB.projection(TestProjection.class);
		assertThat(action, allOf(called(2L), lastArgument(allOf(delegateFirstStrategy(), delegateOf(nodeB), singleArgumentOf(knownAB)))));
	}

	@Test
	default void canMatchProjectionNodeAndKnownObjectUsingBiClosure() {
		val action = ClosureTestUtils.mockClosure(ModelNode.class, KnownDomainObject.class);
		val subject = createSubject();
		val nodeA = subject.node("a");
		val knownA = nodeA.projection(TestProjection.class);
		subject.all(ofType(TestProjection.class), action);
		assertThat(action, calledOnceWith(allOf(delegateFirstStrategy(), delegateOf(nodeA), firstArgumentOf(nodeA), secondArgumentOf(knownA))));

		val nodeB = nodeA.node("b");
		val knownAB = nodeB.projection(TestProjection.class);
		assertThat(action, allOf(called(2L), lastArgument(allOf(delegateFirstStrategy(), delegateOf(nodeB), firstArgumentOf(nodeB), secondArgumentOf(knownAB)))));
	}

	@Test
	default void canMatchKnownObjectsAsProvider() {
		val subject = createSubject();
		val nodeA = subject.node("a");
		val knownA = nodeA.projection(TestProjection.class);
		val provider = subject.all(ofType(TestProjection.class));
		assertThat(provider, providerOf(contains(knownA.map(it -> it).get())));

		val nodeB = nodeA.node("b");
		val knownAB = nodeB.projection(TestProjection.class);
		assertThat(provider, providerOf(contains(knownA.map(it -> it).get(), knownAB.map(it -> it).get())));
	}
}
