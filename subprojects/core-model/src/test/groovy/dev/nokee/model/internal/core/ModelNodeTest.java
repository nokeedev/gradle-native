package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelNodeTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private static final ModelType<WrongType> WRONG_TYPE = ModelType.of(WrongType.class);
	private final ModelProjection projection1 = mock(ModelProjection.class);
	private final ModelProjection projection2 = mock(ModelProjection.class);
	private final ModelProjection projection3 = mock(ModelProjection.class);
	private final ModelNode subject = node("po.ta.to", projection1, projection2, projection3);

	@ParameterizedTest
	@EnumSource(Get.class)
	void returnFirstProjectionMatchingType(GetMethod get) {
		val expectedInstance = TestUtils.objectFactory().newInstance(MyType.class);
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
				return target.get(ModelType.of(type));
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
		assertTrue(node(projectionOf(MyType.class)).canBeViewedAs(TYPE));
		assertFalse(node(projectionOf(MyType.class)).canBeViewedAs(WRONG_TYPE));
	}

	@Test
	void stateOfNewlyCreatedNodeIsInitialized() {
		assertEquals(ModelNode.State.Initialized, node().getState());
	}

	@Test
	void nodeTransitionToRegisteredWhenRegistered() {
		assertEquals(ModelNode.State.Registered, node().register().getState());
	}

	@Test
	void newNodesAreOnlyInitialized() {
		assertTrue(node().isAtLeast(ModelNode.State.Initialized));
		assertFalse(node().isAtLeast(ModelNode.State.Registered));
		assertFalse(node().isAtLeast(ModelNode.State.Realized));
	}

	@Test
	void registeredNodesAreAtMostRegistered() {
		assertTrue(node().register().isAtLeast(ModelNode.State.Initialized));
		assertTrue(node().register().isAtLeast(ModelNode.State.Registered));
		assertFalse(node().register().isAtLeast(ModelNode.State.Realized));
	}

	@Test
	void realizedNodesAreAtMostRealized() {
		assertTrue(node().realize().isAtLeast(ModelNode.State.Initialized));
		assertTrue(node().realize().isAtLeast(ModelNode.State.Registered));
		assertTrue(node().realize().isAtLeast(ModelNode.State.Realized));
	}

	@Nested
	class ModelNodeListenerContractTest {
		private final ModelNodeListener listener = mock(ModelNodeListener.class);
		private final ModelNode node = node(listener);

		@Test
		void callsBackWhenTheNodeIsInitialized() {
			verify(listener, only()).initialized(node);
		}

		@Nested
		class Register {
			@BeforeEach
			void resetListenerMock() {
				Mockito.reset(listener);
			}

			@Test
			void callsBackWhenTheNodeIsRegistered() {
				node.register();
			}

			@Test
			void callsBackOnlyOnceWhenMultipleRegister() {
				node.register().register().register();
			}

			@AfterEach
			void verifyRegisteredCalledOnlyOnce() {
				verify(listener, only()).registered(node);
			}
		}


		@Nested
		class Realize {
			@BeforeEach
			void resetListenerMock() {
				node.register();
				Mockito.reset(listener);
			}

			@Test
			void callsBackWhenTheNodeIsRealized() {
				node.realize();
			}

			@Test
			void callsBackOnlyOnceWhenMultipleRealize() {
				node.realize().realize().realize();
			}

			@Test
			void stayAsRealizeWhenRegisterIsCalledAfter() {
				assertEquals(ModelNode.State.Realized, node.realize().register().getState());
			}

			@AfterEach
			void verifyRegisteredCalledOnlyOnce() {
				verify(listener, only()).realized(node);
			}
		}

		@Nested
		class DirectRealize {
			@BeforeEach
			void realizeNode() {
				Mockito.reset(listener);
				node.realize();
			}

			@Test
			void stateIsRealized() {
				assertEquals(ModelNode.State.Realized, node.getState());
			}

			@Test
			void callsBackThoughRegisteredFollowedByRealized() {
				val inOrder = Mockito.inOrder(listener);
				inOrder.verify(listener, times(1)).registered(node);
				inOrder.verify(listener, times(1)).realized(node);
			}
		}
	}

	@Test
	void canAccessParentNode() {
		val parentNode = node();
		val childNode = childNode(parentNode);
		assertThat(childNode.getParent(), optionalWithValue(equalTo(parentNode)));
	}

	@Test
	void rootNodeHasNoParentNode() {
		assertThat(rootNode().getParent(), emptyOptional());
	}

	@Test
	void parentNodesAreRealize() {
		val parentNode = node();
		val childNode = childNode(parentNode);
		childNode.realize();
		assertAll(() -> {
			assertThat(parentNode.getState(), equalTo(ModelNode.State.Realized));
			assertThat(childNode.getState(), equalTo(ModelNode.State.Realized));
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
		parentNode.getDirectDescendants();
		verify(modelLookup, times(1)).query(allDirectDescendants().scope(path("parent")));
	}

	@Test
	void canRegisterNodeRelativeToCurrentNode() {
		val modelRegistry = mock(ModelRegistry.class);
		val parentNode = childNode(rootNode(), "parent", builder -> builder.withRegistry(modelRegistry));
		parentNode.register(NodeRegistration.of("foo", MyType.class));
		verify(modelRegistry, times(1)).register(ModelRegistration.of("parent.foo", MyType.class));
	}

	interface MyType {}
	interface WrongType {}
}
