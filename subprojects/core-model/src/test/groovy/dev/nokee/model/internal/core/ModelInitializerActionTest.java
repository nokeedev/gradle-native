package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.created;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.initialized;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ModelInitializerAction.class)
class ModelInitializerActionTest {
	@Test
	void executesOnlyIfNodeStateIsCreated() {
		val callCount = new MutableInt();
		node("foo", initialize(context -> callCount.increment()));
		assertThat(callCount.getValue(), equalTo(1));
	}

	@Test
	void canAddUnmanagedProjections() {
		val myType = new MyType();
		val node = node("foo", initialize(context -> context.addProjection(ofInstance(myType))));
		assertTrue(node.canBeViewedAs(of(MyType.class)));
		assertThat(node.get(MyType.class), equalTo(myType));
	}

	@Test
	void canAddManagedProjections() {
		val node = node("foo", initialize(context -> context.addProjection(managed(of(MyManagedType.class)))));
		assertTrue(node.canBeViewedAs(of(MyManagedType.class)));
		assertThat(node.get(MyManagedType.class), isA(MyManagedType.class));
	}

	@Test
	void canApplyActionToNode() {
		val captor = new ModelTestActions.CaptureNodeTransitionAction();
		val node = node("foo", initialize(context -> context.applyTo(self().apply(captor))));
		assertThat(captor.getAllTransitions(), contains(created("foo"), initialized("foo")));
	}

	static final class MyType {}
	interface MyManagedType {}

	private static ModelNode node(String name, ModelAction action) {
		return ModelTestUtils.childNode(ModelTestUtils.rootNode(), name, ImmutableList.of(action), builder -> {});
	}
}
