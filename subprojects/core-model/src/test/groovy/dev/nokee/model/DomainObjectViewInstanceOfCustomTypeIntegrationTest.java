package dev.nokee.model;

import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;

/**
 * It is possible to create custom type of a DomainObjectView.
 */
class DomainObjectViewInstanceOfCustomTypeIntegrationTest {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry(objectFactory()::newInstance);

	@Test
	@Disabled
	void canCreateCustomView() {
		val myTypes = modelRegistry.register(ModelRegistration.of("myTypes", CustomViewOfMyType.class));
		val myType = modelRegistry.register(ModelRegistration.of("myTypes.foo", MyType.class));
		val action = (Action<MyType>) Mockito.mock(Action.class);
		myTypes.get().configureEach(action);
		Mockito.verify(action, Mockito.never()).execute(isA(MyType.class));

		myType.get();

		Mockito.verify(action, times(1)).execute(isA(MyType.class));
	}

	interface MyType {}
	interface CustomViewOfMyType extends DomainObjectView<MyType> {}
}
