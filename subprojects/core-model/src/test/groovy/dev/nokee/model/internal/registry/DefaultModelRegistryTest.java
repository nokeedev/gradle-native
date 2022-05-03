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
package dev.nokee.model.internal.registry;

import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.DescendantNodes;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStateTester;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelNodeUtils.getParent;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultModelRegistryTest {
	private final DefaultModelRegistry subject = new DefaultModelRegistry(ProjectTestUtils.objectFactory()::newInstance);
	private final ModelRegistry modelRegistry = subject;

	@Test
	void cannotRegisterNodeWhenParentDoesNotExists() {
		assertThrows(IllegalArgumentException.class, () -> modelRegistry.register(ModelRegistration.of("foo.bar", MyType.class)));
	}

	@Test
	void canRegisterNestedNodeWhenAncestorExists() {
		assertDoesNotThrow(() -> {
			modelRegistry.register(ModelRegistration.of("foo", MyType.class));
			modelRegistry.register(ModelRegistration.of("foo.bar", MyType.class));
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(modelRegistry);
	}

	@Nested
	class ModelLookupContractTest {
		private final ModelLookup modelLookup = subject;

		private ModelPath register(String path) {
			subject.register(ModelRegistration.of(path, MyType.class));
			return path(path);
		}

		@Test
		void canAccessRootNode() {
			val rootNode = assertDoesNotThrow(() -> modelLookup.get(root()));
			assertEquals(root(), ModelNodeUtils.getPath(rootNode), "node path should be root path");
		}

		@Test
		void rootNodeIsAlwaysRegistered() {
			assertEquals(ModelState.Registered, ModelStates.getState(modelLookup.get(root())));
		}

		@Test
		void registeredNodeAreAtRegisteredState() {
			assertEquals(ModelState.Registered, ModelStates.getState(modelLookup.get(register("woot"))),
				"new node state should be registered");
		}

		@Test
		void throwsExceptionForUnregisteredModelNodeLookup() {
			assertThrows(IllegalArgumentException.class, () -> modelLookup.get(path("foo")));
			assertDoesNotThrow(() -> modelLookup.get(register("foo")));
		}

		@Test
		void failsLookupForInitializedNode() {
			val action = Mockito.mock(ModelAction.class);
			doAnswer(invocation -> {
				assertEquals(path("foo"), ModelNodeUtils.getPath(invocation.getArgument(0, ModelNode.class)));
				assertThrows(IllegalArgumentException.class, () -> modelLookup.get(path("foo")));
				return null;
			}).when(action).execute(any());
			subject.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.class), (node, state) -> {
				if (state.equals(ModelState.Initialized)) {
					action.execute(node);
				}
			}));
			register("foo");
			verify(action, times(1)).execute(any());
		}

		@Test
		void succeedLookupForRegisteredNode() {
			val action = Mockito.mock(ModelAction.class);
			doAnswer(invocation -> {
				assertEquals(path("bar"), ModelNodeUtils.getPath(invocation.getArgument(0, ModelNode.class)));
				assertDoesNotThrow(() -> modelLookup.get(path("bar")));
				return null;
			}).when(action).execute(any());
			subject.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), (node, path, state) -> {
				if (state.equals(ModelState.Registered) && path.get().equals(path("bar"))) {
					action.execute(node);
				}
			}));
			register("bar");
			verify(action, times(1)).execute(any());
		}

		@Test
		void rootNodeAlwaysExists() {
			assertTrue(modelLookup.has(root()), "root node always exists");
		}

		@Test
		void canCheckNonExistingNode() {
			assertFalse(modelLookup.has(path("foo")), "non-existing node does not exists");
		}

		@Test
		void canCheckExistingNode() {
			assertTrue(modelLookup.has(register("foo")), "existing node exists");
		}

		@Test
		void canMatchNode() {
			register("bar");
			assertTrue(modelLookup.anyMatch(ModelSpecs.of(withType(of(MyType.class)))), "a node should match");
			assertFalse(modelLookup.anyMatch(ModelSpecs.of(withType(of(WrongType.class)))), "a node should not match");
		}
	}

	@Test
	void rootEntityHasNoParent() {
		assertThat(getParent(subject.get(root())), emptyOptional());
	}

	@Nested
	class NewEntityParentTest {
		@BeforeEach
		void setUp() {
			modelRegistry.register(ModelRegistration.of("dkij", MyType.class));
			modelRegistry.register(ModelRegistration.of("dkij.koel", MyType.class));
		}

		@Test
		void newEntityHasParent() {
			assertThat(getParent(subject.get(path("dkij"))), optionalWithValue(is(subject.get(root()))));
			assertThat(getParent(subject.get(path("dkij.koel"))), optionalWithValue(is(subject.get(path("dkij")))));
		}
	}

	@Nested
	class RootEntityStateTest implements ModelStateTester.Registered {
		@Override
		public ModelNode subject() {
			return subject.get(root());
		}
	}

	@Nested
	class NewEntityStateTest implements ModelStateTester.Registered {
		@BeforeEach
		void setUp() {
			modelRegistry.register(ModelRegistration.of("djtg", MyType.class));
		}

		@Override
		public ModelNode subject() {
			return subject.get(path("djtg"));
		}
	}

	@Test
	void rootEntityHasDescendantNodesComponent() {
		assertTrue(subject.get(root()).has(DescendantNodes.class));
	}

	@Test
	void newEntityHasDescendantNodesComponent() {
		modelRegistry.register(ModelRegistration.of("kled", MyType.class));
		assertTrue(subject.get(path("kled")).has(DescendantNodes.class));
	}

	interface MyType {}
	interface WrongType {}
}
