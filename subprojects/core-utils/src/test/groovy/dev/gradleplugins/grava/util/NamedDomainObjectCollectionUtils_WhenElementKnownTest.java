package dev.gradleplugins.grava.util;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

import static dev.gradleplugins.grava.util.ActionUtils.onlyIf;
import static dev.gradleplugins.grava.util.NamedDomainObjectCollectionUtils.ElementInfo;
import static dev.gradleplugins.grava.util.NamedDomainObjectCollectionUtils.whenElementKnown;
import static dev.gradleplugins.grava.util.SpecUtils.compose;
import static dev.gradleplugins.grava.util.SpecUtils.subtypeOf;
import static dev.nokee.internal.testing.utils.TestUtils.objectFactory;
import static dev.nokee.utils.Cast.uncheckedCastBecauseOfTypeErasure;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NamedDomainObjectCollectionUtils_WhenElementKnownTest {
	private static PolymorphicDomainObjectContainer<Element> anEmptyContainer() {
		val result = objectFactory().polymorphicDomainObjectContainer(Element.class);
		((DefaultPolymorphicDomainObjectContainer<Element>) result)
			.registerDefaultFactory(name -> objectFactory().newInstance(DefaultElement.class, name));
		result.registerFactory(ChildAElement.class, name -> objectFactory().newInstance(DefaultChildAElement.class, name));
		result.registerFactory(ChildBElement.class, name -> objectFactory().newInstance(DefaultChildBElement.class, name));
		return result;
	}

	@Test
	void doesNotCallActionOnEmptyCollection() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		whenElementKnown(anEmptyContainer(), action);
		verify(action, never()).execute(any());
	}

	@Test
	void callActionOnAnyElementSubsequentlyCreated() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		val container = anEmptyContainer();
		whenElementKnown(container, action);
		container.create("e0");
		container.create("e1", Element.class);
		container.create("e2", ChildAElement.class);
		verify(action, times(3)).execute(any());
	}

	@Test
	void callActionOnAnyElementSubsequentlyRegistered() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		val container = anEmptyContainer();
		whenElementKnown(container, action);
		container.register("e0");
		container.register("e1", Element.class);
		container.register("e2", ChildAElement.class);
		verify(action, times(3)).execute(any());
	}

	@Test
	void callActionOnAnyElementPreviouslyRegistered() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		val container = anEmptyContainer();
		container.register("e0");
		container.register("e1", Element.class);
		container.register("e2", ChildAElement.class);
		whenElementKnown(container, action);
		verify(action, times(3)).execute(any());
	}

	@Test
	void callActionOnAnyElementPreviouslyCreated() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		val container = anEmptyContainer();
		container.create("e0");
		container.create("e1", Element.class);
		container.create("e2", ChildAElement.class);
		whenElementKnown(container, action);
		verify(action, times(3)).execute(any());
	}

	@Test
	void canFilterKnownElementsByName() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		val container = anEmptyContainer();
		container.create("e0");
		container.create("e1", Element.class);
		container.create("e2", ChildAElement.class);
		whenElementKnown(container, onlyIf(it -> it.getName().equals("e1"), action));
		verify(action, times(1)).execute(any());
	}

	@Test
	void canFilterKnownElementsByType() {
		Action<NamedDomainObjectCollectionUtils.ElementInfo<Element>> action = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Action.class));
		val container = anEmptyContainer();
		container.create("e0");
		container.create("e1", Element.class);
		container.create("e2", ChildAElement.class);
		whenElementKnown(container, onlyIf(compose(subtypeOf(ChildAElement.class), ElementInfo::getType), action));
		verify(action, times(1)).execute(any());
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
}
