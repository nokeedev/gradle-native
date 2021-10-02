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
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestUtils.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelNodeTest {
	private static final ModelType<MyType> TYPE = of(MyType.class);
	private static final ModelType<WrongType> WRONG_TYPE = of(WrongType.class);
	private final ModelProjection projection1 = mock(ModelProjection.class);
	private final ModelProjection projection2 = mock(ModelProjection.class);
	private final ModelProjection projection3 = mock(ModelProjection.class);
	private final ModelNode subject = node("po.ta.to", projection1, projection2, projection3);

	@ParameterizedTest
	@EnumSource(Get.class)
	void returnFirstProjectionMatchingType(GetMethod get) {
		val expectedInstance = ProjectTestUtils.objectFactory().newInstance(MyType.class);
		when(projection2.canBeViewedAs(TYPE)).thenReturn(true);
		when(projection2.get(TYPE)).thenReturn(expectedInstance);

		val actualInstance = get.invoke(subject, MyType.class);

		assertEquals(expectedInstance, actualInstance);
		verify(projection1, never()).get(any());
		verify(projection2, times(1)).get(TYPE);
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
				return target.get(of(type));
			}
		},
		GET_USING_CLASS() {
			@Override
			public <T> T invoke(ModelNode target, Class<T> type) {
				return target.get(type);
			}
		};
	}

	@Test
	void canQueryModelNodePath() {
		assertEquals(path("po.ta.to"), node("po.ta.to").getPath());
	}

	@Test
	void canCheckProjectedTypeCompatibility() {
		assertTrue(ModelNodeUtils.canBeViewedAs(node(projectionOf(MyType.class)), TYPE));
		assertFalse(ModelNodeUtils.canBeViewedAs(node(projectionOf(MyType.class)), WRONG_TYPE));
	}

	@Test
	void stateOfNewlyCreatedNodeIsInitialized() {
		assertEquals(ModelNode.State.Initialized, ModelNodeUtils.getState(node()));
	}

	@Test
	void nodeTransitionToRegisteredWhenRegistered() {
		assertEquals(ModelNode.State.Registered, ModelNodeUtils.getState(ModelNodeUtils.register(node())));
	}

	@Test
	void newNodesAreOnlyInitialized() {
		assertTrue(ModelNodeUtils.isAtLeast(node(), ModelNode.State.Initialized));
		assertFalse(ModelNodeUtils.isAtLeast(node(), ModelNode.State.Registered));
		assertFalse(ModelNodeUtils.isAtLeast(node(), ModelNode.State.Realized));
	}

	@Test
	void registeredNodesAreAtMostRegistered() {
		assertTrue(ModelNodeUtils.isAtLeast(ModelNodeUtils.register(node()), ModelNode.State.Initialized));
		assertTrue(ModelNodeUtils.isAtLeast(ModelNodeUtils.register(node()), ModelNode.State.Registered));
		assertFalse(ModelNodeUtils.isAtLeast(ModelNodeUtils.register(node()), ModelNode.State.Realized));
	}

	@Test
	void realizedNodesAreAtMostRealized() {
		assertTrue(ModelNodeUtils.isAtLeast(ModelNodeUtils.realize(node()), ModelNode.State.Initialized));
		assertTrue(ModelNodeUtils.isAtLeast(ModelNodeUtils.realize(node()), ModelNode.State.Registered));
		assertTrue(ModelNodeUtils.isAtLeast(ModelNodeUtils.realize(node()), ModelNode.State.Realized));
	}

	@Nested
	class ModelNodeListenerContractTest {
		private final ModelNodeListener listener = mock(ModelNodeListener.class);
		private final ModelNode node = node(listener);

		@Test
		void callsBackWhenTheNodeIsCreated() {
			verify(listener, times(1)).created(node);
		}

		@Test
		void callsBackCreatedBeforeInitialized() {
			val inOrder = inOrder(listener);
			inOrder.verify(listener, times(1)).created(node);
			inOrder.verify(listener, times(1)).initialized(node);
		}

		@Test
		void callsBackWhenTheNodeIsInitialized() {
			verify(listener, times(1)).initialized(node);
		}

		@Nested
		class Register {
			@BeforeEach
			void resetListenerMock() {
				Mockito.reset(listener);
			}

			@Test
			void callsBackWhenTheNodeIsRegistered() {
				ModelNodeUtils.register(node);
			}

			@Test
			void callsBackOnlyOnceWhenMultipleRegister() {
				ModelNodeUtils.register(ModelNodeUtils.register(ModelNodeUtils.register(node)));
			}

			@AfterEach
			void verifyRegisteredCalledOnlyOnce() {
				verify(listener).registered(node);
			}
		}


		@Nested
		class Realize {
			@BeforeEach
			void resetListenerMock() {
				ModelNodeUtils.register(node);
				Mockito.reset(listener);
			}

			@Test
			void callsBackWhenTheNodeIsRealized() {
				ModelNodeUtils.realize(node);
			}

			@Test
			void callsBackOnlyOnceWhenMultipleRealize() {
				ModelNodeUtils.realize(ModelNodeUtils.realize(ModelNodeUtils.realize(node)));
			}

			@Test
			void stayAsRealizeWhenRegisterIsCalledAfter() {
				assertEquals(ModelNode.State.Realized, ModelNodeUtils.getState(ModelNodeUtils.register(ModelNodeUtils.realize(node))));
			}

			@AfterEach
			void verifyRegisteredCalledOnlyOnce() {
				verify(listener).realized(node);
			}
		}

		@Nested
		class DirectRealize {
			@BeforeEach
			void realizeNode() {
				Mockito.reset(listener);
				ModelNodeUtils.realize(node);
			}

			@Test
			void stateIsRealized() {
				assertEquals(ModelNode.State.Realized, ModelNodeUtils.getState(node));
			}

			@Test
			void callsBackThoughRegisteredFollowedByRealized() {
				val inOrder = inOrder(listener);
				inOrder.verify(listener, times(1)).registered(node);
				inOrder.verify(listener, times(1)).realized(node);
			}
		}
	}

	@Test
	void canAccessParentNode() {
		val parentNode = node();
		val childNode = childNode(parentNode);
		assertThat(ModelNodeUtils.getParent(childNode), optionalWithValue(equalTo(parentNode)));
	}

	@Test
	void rootNodeHasNoParentNode() {
		assertThat(ModelNodeUtils.getParent(rootNode()), emptyOptional());
	}

	@Test
	void parentNodesAreRealize() {
		val parentNode = node();
		val childNode = childNode(parentNode);
		ModelNodeUtils.realize(childNode);
		assertAll(() -> {
			assertThat(ModelNodeUtils.getState(parentNode), equalTo(ModelNode.State.Realized));
			assertThat(ModelNodeUtils.getState(childNode), equalTo(ModelNode.State.Realized));
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
	void canRegisterNodeRelativeToCurrentNode() {
		val modelRegistry = mock(ModelRegistry.class);
		val parentNode = childNode(rootNode(), "parent", builder -> builder.withRegistry(modelRegistry));
		parentNode.register(NodeRegistration.of("foo", of(MyType.class)));
		verify(modelRegistry, times(1)).register(ModelRegistration.of("parent.foo", MyType.class));
	}

	@Test
	void canApplyConfigurationToSelf() {
		val modelConfigurer = mock(ModelConfigurer.class);
		val node = node("foo", builder -> builder.withConfigurer(modelConfigurer));
		node.applyTo(self().apply(doSomething()));
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
	interface WrongType {}
}
