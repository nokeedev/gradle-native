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
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.ArrayList;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.realized;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ModelMutateAction.class)
class ModelMutateActionTest {
	@Test
	void executesOnlyIfNodeStateIsRealized() {
		assertThat(executeWith(consumer(action -> node(mutate(action)))), calledOnce());
	}

	@Test
	void canApplyActionToNode() {
		val captor = new ModelTestActions.CaptureNodeTransitionAction();
		node("foo", mutate(context -> context.applyTo(self().apply(captor))));
		assertThat(captor.getAllTransitions(), contains(realized("foo")));
	}

	@Test
	void canAccessNodePath() {
		val paths = new ArrayList<ModelPath>();
		node("foo", mutate(context -> paths.add(context.getPath())));
		assertThat(paths, contains(path("foo")));
	}

	@Test
	void throwsExceptionWhenAccessingUnknownProjection() {
		assertThrows(IllegalArgumentException.class,
			() -> node(mutate(context -> context.projectionOf(of(MyType.class)))));
	}

	@Test
	void canAccessKnownProjection() {
		val capturedValue = new MutableObject<MyType>();
		assertDoesNotThrow(() -> node(
			initialize(context -> context.withProjection(managed(of(MyType.class)))),
			mutate(context -> capturedValue.setValue(context.projectionOf(of(MyType.class))))));
		assertThat(capturedValue.getValue(), isA(MyType.class));
	}

	@Test
	void canAccessTheNodeContextually() {
		val capturedValue = new MutableObject<ModelNode>();
		val expected = node(mutate(context -> capturedValue.setValue(ModelNodeContext.getCurrentModelNode())));
		assertThat(capturedValue.getValue(), equalTo(expected));
	}

	interface MyType {}

	private static ModelNode node(ModelAction... action) {
		return ModelNodeUtils.realize(ModelTestUtils.childNode(ModelTestUtils.rootNode(), "test", ImmutableList.copyOf(action), builder -> {}));
	}

	private static ModelNode node(String name, ModelAction... action) {
		return ModelNodeUtils.realize(ModelTestUtils.childNode(ModelTestUtils.rootNode(), name, ImmutableList.copyOf(action), builder -> {}));
	}
}
