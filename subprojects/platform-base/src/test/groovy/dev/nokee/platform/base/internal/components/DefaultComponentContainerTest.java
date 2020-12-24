package dev.nokee.platform.base.internal.components;

import dev.nokee.internal.Factories;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.internal.BaseNamedDomainObjectContainer;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.testing.base.TestSuiteComponent;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(DefaultComponentContainer.class)
class DefaultComponentContainerTest {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry(TestUtils.objectFactory()::newInstance);
	private final ComponentContainer subject = modelRegistry.register(namedContainer("components", of(DefaultComponentContainer.class))).get();

	private static <T> DomainObjectFactory<T> alwaysThrow() {
		return t -> Factories.<T>alwaysThrow().create();
	}

	@Test
	void throwsExceptionWhenRegisteringTestSuiteComponentFactory() {
		val ex = assertThrows(IllegalArgumentException.class, () -> subject.registerFactory(MyTestSuiteComponent.class, alwaysThrow()));
		assertThat(ex.getMessage(), equalTo("Cannot register test suite component types in this container, use a TestSuiteContainer instead."));
	}

	@Test
	void throwsExceptionWhenBindingTestSuiteComponentToComponentType() {
		subject.registerFactory(MyComponent.class, alwaysThrow());
		val ex = assertThrows(IllegalArgumentException.class, () -> subject.registerBinding(MyBaseTestSuiteComponent.class, MyBaseTestSuiteComponent.class));
		assertThat(ex.getMessage(), equalTo("Cannot bind test suite component types in this container, use a TestSuiteContainer instead."));
	}

	@Test
	void throwsExceptionWhenRegisteringTestSuiteComponents() {
		assertAll(() -> {
			val ex = assertThrows(IllegalArgumentException.class, () -> subject.register("main", MyTestSuiteComponent.class));
			assertThat(ex.getMessage(), equalTo("Cannot register test suite components in this container, use a TestSuiteContainer instead."));
		});

		assertAll(() -> {
			val ex = assertThrows(IllegalArgumentException.class, () -> subject.register("main", MyTestSuiteComponent.class, doNothing()));
			assertThat(ex.getMessage(), equalTo("Cannot register test suite components in this container, use a TestSuiteContainer instead."));
		});
	}

	interface MyComponent extends Component {}
	interface MyBaseTestSuiteComponent extends TestSuiteComponent {}
	static class MyTestSuiteComponent implements MyBaseTestSuiteComponent {
		@Override
		public TestSuiteComponent testedComponent(Object component) {
			throw new UnsupportedOperationException();
		}
	}
}
