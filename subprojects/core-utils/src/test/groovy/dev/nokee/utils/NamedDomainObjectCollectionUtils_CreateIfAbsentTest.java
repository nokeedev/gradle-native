package dev.nokee.utils;

import lombok.val;
import org.gradle.api.*;
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.inject.Inject;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ActionTestUtils.doSomething;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.createIfAbsent;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.registerIfAbsent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NamedDomainObjectCollectionUtils_CreateIfAbsentTest {
	private static PolymorphicDomainObjectContainer<Element> aContainerWithExistingElements() {
		val result = anEmptyContainer();
		result.create("existing");
		return result;
	}

	private static PolymorphicDomainObjectContainer<Element> anEmptyContainer() {
		val result = objectFactory().polymorphicDomainObjectContainer(Element.class);
		((DefaultPolymorphicDomainObjectContainer<Element>) result)
			.registerDefaultFactory(name -> objectFactory().newInstance(DefaultElement.class, name));
		result.registerFactory(ChildAElement.class, name -> objectFactory().newInstance(DefaultChildAElement.class, name));
		result.registerFactory(ChildBElement.class, name -> objectFactory().newInstance(DefaultChildBElement.class, name));
		return result;
	}

	private static NamedDomainObjectContainer<Element> aContainer(Consumer<? super NamedDomainObjectContainer<Element>> action) {
		val result = anEmptyContainer();
		action.accept(result);
		return result;
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void canCreateMissingElements(CreateMethod method) {
		assertThat(aContainer(c -> method.invoke(c, "e1", doSomething())), contains(named("e1")));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void returnExistingElements(CreateMethod method) {
		val element = assertDoesNotThrow(() -> method.invoke(aContainerWithExistingElements(), "existing", doSomething()));
		assertThat(element, named("existing"));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void executesActionWhenCreatingMissingElement(CreateMethod method) {
		val execution = executeWith(action(it -> method.invoke(anEmptyContainer(), "e2", it)));
		assertThat(execution, calledOnce());
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void doesNotExecuteActionWhenElementExists(CreateMethod method) {
		val execution = executeWith(action(it -> method.invoke(aContainerWithExistingElements(), "existing", it)));
		assertThat(execution, neverCalled());
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void doesNotTriggerContainerRulesWhenElementIsAbsent(CreateMethod method) {
		assertDoesNotThrow(() -> method.invoke(aContainerWithThrowingRule(), "e3", doSomething()));
	}

	private static NamedDomainObjectContainer<Element> aContainerWithThrowingRule() {
		val container = anEmptyContainer();
		container.addRule("always throws", name -> { throw new UnsupportedOperationException(); });
		return container;
	}

	@Test
	void doesNotThrowExceptionWhenCreateTypeIsSuperTypeOfExistingElement() {
		val container = anEmptyContainer();
		container.create("existing", ChildAElement.class);
		assertDoesNotThrow(() -> createIfAbsent(container, "existing", Element.class, doSomething()));
	}

	@Test
	void throwsExceptionWhenCreateTypeIsUnrelatedTypeOfExistingElement() {
		val container = anEmptyContainer();
		container.create("existing", ChildAElement.class);
		val ex = assertThrows(InvalidUserDataException.class,
			() -> createIfAbsent(container, "existing", ChildBElement.class, doSomething()));
		assertThat(ex.getMessage(),
			equalTo("Could not register element 'existing': Element type requested (dev.nokee.utils.NamedDomainObjectCollectionUtils_CreateIfAbsentTest.ChildBElement) does not match actual type (dev.nokee.utils.NamedDomainObjectCollectionUtils_CreateIfAbsentTest.Element)."));
	}

	@Test
	void throwsExceptionWhenCreateTypeIsChildTypeOfExistingElement() {
		val container = anEmptyContainer();
		container.create("existing", Element.class);
		val ex = assertThrows(InvalidUserDataException.class,
			() -> createIfAbsent(container, "existing", ChildBElement.class, doSomething()));
		assertThat(ex.getMessage(),
			equalTo("Could not register element 'existing': Element type requested (dev.nokee.utils.NamedDomainObjectCollectionUtils_CreateIfAbsentTest.ChildBElement) does not match actual type (dev.nokee.utils.NamedDomainObjectCollectionUtils_CreateIfAbsentTest.Element)."));
	}

	protected interface Element extends Named {}
	protected interface ChildAElement extends Element {}
	protected interface ChildBElement extends Element {}

	protected static class DefaultElement implements Element {
		private final String name;

		@Inject
		public DefaultElement(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

	protected static class DefaultChildAElement extends DefaultElement implements ChildAElement {
		@Inject
		public DefaultChildAElement(String name) {
			super(name);
		}
	}

	protected static class DefaultChildBElement extends DefaultElement implements ChildBElement {
		@Inject
		public DefaultChildBElement(String name) {
			super(name);
		}
	}

	private enum CreateMethod {
		CreateIfAbsent {
			@Override
			<T> T invoke(NamedDomainObjectContainer<T> self, String name, Action<? super T> action) {
				return createIfAbsent(self, name, action);
			}
		},
		RegisterIfAbsent {
			@Override
			<T> T invoke(NamedDomainObjectContainer<T> self, String name, Action<? super T> action) {
				return registerIfAbsent(self, name, action).get();
			}
		};

		<T> T invoke(NamedDomainObjectContainer<T> self, String name) {
			return invoke(self, name, doSomething());
		}

		abstract <T> T invoke(NamedDomainObjectContainer<T> self, String name, Action<? super T> action);
	}
}
