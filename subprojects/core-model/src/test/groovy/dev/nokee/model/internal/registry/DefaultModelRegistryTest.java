package dev.nokee.model.internal.registry;

import com.google.common.testing.NullPointerTester;
import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.*;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
			val rootNode = assertDoesNotThrow(() -> modelLookup.get(ModelPath.root()));
			assertEquals(ModelPath.root(), rootNode.getPath(), "node path should be root path");
		}

		@Test
		void rootNodeIsAlwaysRegistered() {
			assertEquals(ModelNode.State.Registered, modelLookup.get(ModelPath.root()).getState());
		}

		@Test
		void registeredNodeAreAtRegisteredState() {
			assertEquals(ModelNode.State.Registered, modelLookup.get(register("woot")).getState(), "new node state should be registered");
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
				assertEquals(path("foo"), invocation.getArgument(0, ModelNode.class).getPath());
				assertThrows(IllegalArgumentException.class, () -> modelLookup.get(path("foo")));
				return null;
			}).when(action).execute(any());
			subject.configure(matching(it -> it.getState().equals(ModelNode.State.Initialized), action));
			register("foo");
			verify(action, times(1)).execute(any());
		}

		@Test
		void succeedLookupForRegisteredNode() {
			val action = Mockito.mock(ModelAction.class);
			doAnswer(invocation -> {
				assertEquals(path("bar"), invocation.getArgument(0, ModelNode.class).getPath());
				assertDoesNotThrow(() -> modelLookup.get(path("bar")));
				return null;
			}).when(action).execute(any());
			subject.configure(matching(it -> it.getState().equals(ModelNode.State.Registered) && it.getPath().equals(path("bar")), action));
			register("bar");
			verify(action, times(1)).execute(any());
		}

		@Test
		void rootNodeAlwaysExists() {
			assertTrue(modelLookup.has(ModelPath.root()), "root node always exists");
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
	void canRegisterNodeRelativeToRootNode() {
		assertThat("relative registration are performed from the root node",
			subject.register(NodeRegistration.of("foo", of(MyType.class))), equalTo(modelRegistry.get("foo", MyType.class)));
	}

	@Test
	void canRegisterNodeRelativeToNestedNode() {
		subject.register(ModelRegistration.of("a", MyType.class));
		assertThat("relative registration can also be performed from an arbitrary node",
			subject.get(path("a")).register(NodeRegistration.of("foo", of(MyType.class))),
			equalTo(modelRegistry.get("a.foo", MyType.class)));
	}

	interface MyType {}
	interface WrongType {}
}
