/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.ArrayList;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.realized;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.registered;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ModelDiscoverAction.class)
class ModelDiscoverActionTest {
	@Test
	void executesOnlyIfNodeStateIsRegistered() {
		assertThat(executeWith(consumer(action -> node(discover(action)))),
			calledOnce());
	}

	@Test
	void canRegisterChildNode() {
		val node = node(discover(context -> context.register(NodeRegistration.of("bar", of(MyType.class)))));
		assertThat(ModelNodeUtils.hasDescendant(node, "bar"), equalTo(true));
	}

	@Test
	void returnKnownDomainObjectWhenRegisteringChildNode() {
		val capturedValue = new MutableObject<ModelElement>();
		val node = node(discover(context -> capturedValue.setValue(context.register(NodeRegistration.of("bar", of(MyType.class))))));
		assertThat(capturedValue.getValue().as(MyType.class).getType(), equalTo(MyType.class));
	}

	@Test
	void canApplyActionToNode() {
		val captor = new ModelTestActions.CaptureNodeTransitionAction();
		val node = node("foo", discover(context -> context.applyTo(self().apply(captor))));
		assertThat(captor.getAllTransitions(), contains(registered("foo")));
		ModelStates.realize(node);
		assertThat(captor.getAllTransitions(), contains(registered("foo"), realized("foo")));
	}

	@Test
	void canAccessNodePath() {
		val paths = new ArrayList<ModelPath>();
		node("foo", discover(context -> paths.add(context.getPath())));
		assertThat(paths, contains(path("foo")));
	}

	@Test
	void throwsExceptionWhenAccessingUnknownProjection() {
		assertThrows(IllegalArgumentException.class,
			() -> node(discover(context -> context.projectionOf(of(MyType.class)))));
	}

	@Test
	void canAccessKnownProjection() {
		val capturedValue = new MutableObject<KnownDomainObject<MyType>>();
		assertDoesNotThrow(() -> node(
			initialize(context -> context.withProjection(managed(of(MyType.class)))),
			discover(context -> capturedValue.setValue(context.projectionOf(of(MyType.class))))));
		assertThat(capturedValue.getValue().getType(), equalTo(MyType.class));
	}

	@Test
	void canAccessTheNodeContextually() {
		val capturedValue = new MutableObject<ModelNode>();
		val expected = node(discover(context -> capturedValue.setValue(ModelNodeContext.getCurrentModelNode())));
		assertThat(capturedValue.getValue(), equalTo(expected));
	}

	interface MyType {}

	private static ModelNode node(ModelAction... action) {
		return ModelStates.register(ModelTestUtils.childNode(ModelTestUtils.rootNode(), "test", ImmutableList.copyOf(action), builder -> {}));
	}

	private static ModelNode node(String name, ModelAction... action) {
		return ModelStates.register(ModelTestUtils.childNode(ModelTestUtils.rootNode(), name, ImmutableList.copyOf(action), builder -> {}));
	}
}
