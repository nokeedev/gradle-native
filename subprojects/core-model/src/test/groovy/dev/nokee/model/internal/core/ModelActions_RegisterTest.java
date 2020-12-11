package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(ModelActions.class)
class ModelActions_RegisterTest {
	@Test
	void registerNodeOnExecutingNode() {
		val modelRegistry = mock(ModelRegistry.class);
		val node = node("foo", builder -> builder.withRegistry(modelRegistry));
		register(NodeRegistration.of("bar", MyType.class)).execute(node);
		verify(modelRegistry, times(1)).register(ModelRegistration.of("foo.bar", MyType.class));
	}

	@Test
	void checkToString() {
		assertThat(register(NodeRegistration.of("bar", MyType.class)), hasToString("ModelActions.register(NodeRegistration(name=bar, type=interface dev.nokee.model.internal.core.ModelActions_RegisterTest$MyType, projections=[], actions=[]))"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(register(NodeRegistration.of("bar", MyType.class)), register(NodeRegistration.of("bar", MyType.class)))
			.addEqualityGroup(register(NodeRegistration.of("foo", MyType.class)))
			.addEqualityGroup(register(NodeRegistration.of("bar", MyOtherType.class)))
			.testEquals();
	}

	interface MyType {}
	interface MyOtherType {}
}
