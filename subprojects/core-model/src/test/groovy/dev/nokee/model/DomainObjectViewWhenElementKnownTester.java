package dev.nokee.model;

import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;

public abstract class DomainObjectViewWhenElementKnownTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanWhenElementKnownUsingAction extends AbstractDomainObjectViewWhenElementsKnownTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewWhenElementKnownTester.this.getSubjectGenerator();
		}

		@Override
		protected void whenElementKnown(DomainObjectView<T> subject, Consumer<? super KnownDomainObject<T>> action) {
			subject.whenElementKnownEx(action::accept);
		}
	}

	@Nested
	class CanWhenElementKnownUsingClosure extends AbstractDomainObjectViewWhenElementsKnownTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewWhenElementKnownTester.this.getSubjectGenerator();
		}

		@Override
		protected void whenElementKnown(DomainObjectView<T> subject, Consumer<? super KnownDomainObject<T>> action) {
			subject.whenElementKnownEx(adaptToClosure(action));
		}
	}
}
