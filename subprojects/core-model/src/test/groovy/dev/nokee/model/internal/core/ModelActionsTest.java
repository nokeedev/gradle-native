package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.type.ModelType;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

@Subject(ModelActions.class)
class ModelActionsTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.of(MyType.class)).testAllPublicStaticMethods(ModelActions.class);
	}

	interface MyType {}
}
