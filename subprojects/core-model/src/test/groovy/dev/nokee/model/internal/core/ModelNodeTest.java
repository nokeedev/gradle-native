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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestUtils.childNode;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static dev.nokee.model.internal.core.ModelTestUtils.rootNode;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModelNodeTest {
	private static final ModelType<MyType> TYPE = of(MyType.class);
	private static final ModelType<MyType1> TYPE1 = of(MyType1.class);
	private static final ModelType<MyType2> TYPE2 = of(MyType2.class);
	private static final ModelType<MyType3> TYPE3 = of(MyType3.class);
	private static final ModelType<WrongType> WRONG_TYPE = of(WrongType.class);
	private final ModelProjection projection1 = mockProjectionOf(TYPE1);
	private final ModelProjection projection2 = mockProjectionOf(TYPE2);
	private final ModelProjection projection3 = mockProjectionOf(TYPE3);
	private final ModelNode subject = node("po.ta.to", projection1, projection2, projection3);

	private static ModelProjection mockProjectionOf(ModelType<?> type) {
		val result = spy(ModelProjection.class);
		when(result.getType()).thenReturn(Cast.uncheckedCast(type));
		return result;
	}

	@ParameterizedTest
	@EnumSource(Get.class)
	void returnFirstProjectionMatchingType(GetMethod get) {
		val expectedInstance = ProjectTestUtils.objectFactory().newInstance(MyType2.class);
		when(projection2.canBeViewedAs(TYPE2)).thenReturn(true);
		when(projection2.get(TYPE2)).thenReturn(expectedInstance);

		val actualInstance = get.invoke(subject, MyType2.class);

		assertEquals(expectedInstance, actualInstance);
		verify(projection1, never()).get(any());
		verify(projection2, times(1)).get(TYPE2);
		verify(projection3, never()).get(any());
	}

	@ParameterizedTest
	@EnumSource(Get.class)
	void throwsExceptionWhenNoProjectionMatchingType(GetMethod get) {
		assertThrows(IllegalStateException.class, () -> get.invoke(node(projectionOf(WrongType.class)), MyType.class));
	}

	interface GetMethod {
		<T> T invoke(ModelNode target, Class<T> type);
	}
	enum Get implements GetMethod {
		GET_USING_MODEL_TYPE() {
			@Override
			public <T> T invoke(ModelNode target, Class<T> type) {
				return ModelNodeUtils.get(target, of(type));
			}
		},
		GET_USING_CLASS() {
			@Override
			public <T> T invoke(ModelNode target, Class<T> type) {
				return ModelNodeUtils.get(target, type);
			}
		};
	}

	@Test
	void canQueryModelNodePath() {
		assertEquals(path("po.ta.to"), ModelNodeUtils.getPath(node("po.ta.to")));
	}

	@Test
	void canCheckProjectedTypeCompatibility() {
		assertTrue(ModelNodeUtils.canBeViewedAs(node(projectionOf(MyType.class)), TYPE));
		assertFalse(ModelNodeUtils.canBeViewedAs(node(projectionOf(MyType.class)), WRONG_TYPE));
	}

	@Test
	void parentNodesAreRealize() {
		val parentNode = node();
		val childNode = childNode(parentNode);
		ModelStates.realize(childNode);
		assertAll(() -> {
			assertThat(ModelStates.getState(parentNode), equalTo(ModelState.Realized));
			assertThat(ModelStates.getState(childNode), equalTo(ModelState.Realized));
		});
	}

	@Test
	void checkToString() {
		val parentNode = childNode(rootNode(), "foo");
		val childNode = childNode(parentNode, "bar");

		assertThat(rootNode(), hasToString("<root>"));
		assertThat(parentNode, hasToString("foo"));
		assertThat(childNode, hasToString("foo.bar"));
	}

	@Test
	void canQueryAllDirectDescendantsOfNode() {
		val modelLookup = mock(ModelLookup.class);
		val parentNode = childNode(rootNode(), "parent", builder -> builder.withLookup(modelLookup));
		when(modelLookup.query(any())).thenReturn(ModelLookup.Result.empty());
		ModelNodeUtils.getDirectDescendants(parentNode);
		verify(modelLookup, times(1)).query(allDirectDescendants().scope(path("parent")));
	}

	@Test
	void canApplyConfigurationToSelf() {
		val modelConfigurer = mock(ModelConfigurer.class);
		val node = node("foo", builder -> builder.withConfigurer(modelConfigurer));
		ModelNodeUtils.applyTo(node, self().apply(doSomething()));
		verify(modelConfigurer, times(1))
			.configure(self().apply(doSomething()).scope(path("foo")));
	}

	@Test
	void canGetDescendantNode() {
		val modelLookup = mock(ModelLookup.class);
		val node = node("foo", builder -> builder.withLookup(modelLookup));
		ModelNodeUtils.getDescendant(node, "bar");
		verify(modelLookup, times(1)).get(path("foo.bar"));
	}

	@Test
	void canHasDescendantNode() {
		val modelLookup = mock(ModelLookup.class);
		val node = node("foo", builder -> builder.withLookup(modelLookup));
		ModelNodeUtils.hasDescendant(node, "bar");
		verify(modelLookup, times(1)).has(path("foo.bar"));
	}

	@Test
	void canGetTypeDescriptionOfNode() {
		assertThat(ModelNodeUtils.getTypeDescription(node("a", MyType.class)), optionalWithValue(equalTo("interface dev.nokee.model.internal.core.ModelNodeTest$MyType")));
	}

	@Test
	@Disabled // until we completely split the state from the ModelNode because state now "tag" the node using projections...
	void returnsEmptyTypeDescriptionForNodeWithoutProjection() {
		assertThat(ModelNodeUtils.getTypeDescription(node("a")), emptyOptional());
	}

	interface MyType {}
	interface MyType1 {}
	interface MyType2 {}
	interface MyType3 {}
	interface WrongType {}
}
