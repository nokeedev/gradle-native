package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.KnownDomainObject;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.ArrayList;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.created;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.initialized;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Subject(ModelInitializerAction.class)
class ModelInitializerActionTest {
	@Test
	void executesOnlyIfNodeStateIsCreated() {
		assertThat(executeWith(consumer(action -> node(initialize(action)))),
			calledOnce());
	}

	@Test
	void canAddUnmanagedProjections() {
		val capturedValue = new MutableObject<KnownDomainObject<MyType>>();
		val myType = new MyType();
		val node = node(
			initialize(context -> capturedValue.setValue(context.withProjection(ofInstance(myType)))));
		assertTrue(node.canBeViewedAs(of(MyType.class)));
		assertThat(node.get(MyType.class), equalTo(myType));
		assertThat(capturedValue.getValue().getType(), equalTo(MyType.class));
	}

	@Test
	void canAddManagedProjections() {
		val capturedValue = new MutableObject<KnownDomainObject<MyManagedType>>();
		val node = node(
			initialize(context -> capturedValue.setValue(context.withProjection(managed(of(MyManagedType.class))))));
		assertTrue(node.canBeViewedAs(of(MyManagedType.class)));
		assertThat(node.get(MyManagedType.class), isA(MyManagedType.class));
		assertThat(capturedValue.getValue().getType(), equalTo(MyManagedType.class));
	}

	@Test
	void canAddModelProjections() {
		ModelProjection projection = managed(of(MyManagedType.class));
		val node = node(
			initialize(context -> assertThat(context.withProjection(projection), equalTo(context))));
		assertTrue(node.canBeViewedAs(of(MyManagedType.class)));
		assertThat(node.get(MyManagedType.class), isA(MyManagedType.class));
	}

	@Test
	void canApplyActionToNode() {
		val captor = new ModelTestActions.CaptureNodeTransitionAction();
		val node = node("foo", initialize(context -> context.applyTo(self().apply(captor))));
		assertThat(captor.getAllTransitions(), contains(created("foo"), initialized("foo")));
	}

	@Test
	void canAccessNodePath() {
		val paths = new ArrayList<ModelPath>();
		node("foo", initialize(context -> paths.add(context.getPath())));
		assertThat(paths, contains(path("foo")));
	}

	@Test
	void throwsExceptionWhenAccessingUnknownProjection() {
		assertThrows(IllegalArgumentException.class,
			() -> node(initialize(context -> context.projectionOf(of(MyManagedType.class)))));
	}

	@Test
	void canAccessKnownProjection() {
		val capturedValue = new MutableObject<KnownDomainObject<MyManagedType>>();
		ModelProjection projection = managed(of(MyManagedType.class));
		assertDoesNotThrow(() -> node(
			initialize(context -> capturedValue.setValue(context.withProjection(projection).projectionOf(of(MyManagedType.class))))));
		assertThat(capturedValue.getValue().getType(), equalTo(MyManagedType.class));
	}

	@Test
	void canAccessTheNodeContextually() {
		val capturedValue = new MutableObject<ModelNode>();
		val expected = node(initialize(context -> capturedValue.setValue(ModelNodeContext.getCurrentModelNode())));
		assertThat(capturedValue.getValue(), equalTo(expected));
	}

	static final class MyType {}
	interface MyManagedType {}

	private static ModelNode node(ModelAction action) {
		return ModelTestUtils.childNode(ModelTestUtils.rootNode(), "test", ImmutableList.of(action), builder -> {});
	}

	private static ModelNode node(String name, ModelAction action) {
		return ModelTestUtils.childNode(ModelTestUtils.rootNode(), name, ImmutableList.of(action), builder -> {});
	}
}
