package dev.nokee.model.internal.registry;

import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultModelRegistryTest {
	ModelRegistry modelRegistry = new DefaultModelRegistry(TestUtils.objectFactory());

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

	interface MyType {}
}
