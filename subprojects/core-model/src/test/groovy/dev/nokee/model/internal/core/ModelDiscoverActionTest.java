package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spock.lang.Subject;

import java.util.ArrayList;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.realized;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.registered;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Subject(ModelDiscoverAction.class)
class ModelDiscoverActionTest {
	@Test
	void executesOnlyIfNodeStateIsRegistered() {
		val callCount = new MutableInt();
		node("foo", discover(context -> callCount.increment()));
		assertThat(callCount.getValue(), equalTo(1));
	}

	@Test
	void canRegisterChildNode() {
		val modelRegistry = Mockito.mock(ModelRegistry.class);
		node("foo", discover(context -> context.register(NodeRegistration.of("bar", of(MyType.class)))), modelRegistry);
		verify(modelRegistry, times(1)).register(NodeRegistration.of("bar", of(MyType.class)).scope(path("foo")));
	}

	@Test
	void canApplyActionToNode() {
		val captor = new ModelTestActions.CaptureNodeTransitionAction();
		val node = node("foo", discover(context -> context.applyTo(self().apply(captor))));
		assertThat(captor.getAllTransitions(), contains(registered("foo")));
		node.realize();
		assertThat(captor.getAllTransitions(), contains(registered("foo"), realized("foo")));
	}

	@Test
	void canAccessNodePath() {
		val paths = new ArrayList<ModelPath>();
		node("foo", discover(context -> paths.add(context.getPath())));
		assertThat(paths, contains(path("foo")));
	}

	interface MyType {}

	private static ModelNode node(String name, ModelAction action) {
		return ModelTestUtils.childNode(ModelTestUtils.rootNode(), name, ImmutableList.of(action), builder -> {}).register();
	}

	private static ModelNode node(String name, ModelAction action, ModelRegistry modelRegistry) {
		return ModelTestUtils.childNode(ModelTestUtils.rootNode(), name, ImmutableList.of(action), builder -> builder.withRegistry(modelRegistry)).register();
	}
}
