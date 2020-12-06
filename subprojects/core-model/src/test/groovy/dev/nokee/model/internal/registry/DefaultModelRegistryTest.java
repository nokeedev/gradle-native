package dev.nokee.model.internal.registry;

import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelRegistration;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultModelRegistryTest {
	private final DefaultModelRegistry subject = new DefaultModelRegistry(TestUtils.objectFactory());
	private final ModelRegistry modelRegistry = subject;

	@Test
	void cannotRegisterNodeWhenParentDoesNotExists() {
		assertThrows(IllegalArgumentException.class, () -> modelRegistry.register(ModelRegistration.of(ModelIdentifier.of("foo.bar", MyType.class))));
	}

	@Test
	void canRegisterNestedNodeWhenAncestorExists() {
		assertDoesNotThrow(() -> {
			modelRegistry.register(ModelRegistration.of(ModelIdentifier.of("foo", MyType.class)));
			modelRegistry.register(ModelRegistration.of(ModelIdentifier.of("foo.bar", MyType.class)));
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
			subject.register(ModelRegistration.of(path, DefaultModelRegistryIntegrationTest.MyType.class));
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
	}

	interface MyType {}
}
