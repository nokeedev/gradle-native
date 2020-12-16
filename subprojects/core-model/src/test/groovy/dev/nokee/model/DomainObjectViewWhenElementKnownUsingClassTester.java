package dev.nokee.model;

import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;

public abstract class DomainObjectViewWhenElementKnownUsingClassTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanWhenElementKnownUsingAction extends AbstractDomainObjectViewWhenElementsKnownTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewWhenElementKnownUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected void whenElementKnown(DomainObjectView<T> subject, Consumer<? super KnownDomainObject<T>> action) {
			subject.whenElementKnownEx(getElementType(), action::accept);
		}
	}

	@Nested
	class CanWhenElementKnownUsingClosure extends AbstractDomainObjectViewWhenElementsKnownTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewWhenElementKnownUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected void whenElementKnown(DomainObjectView<T> subject, Consumer<? super KnownDomainObject<T>> action) {
			subject.whenElementKnownEx(getElementType(), adaptToClosure(action));
		}
	}

	@Nested
	class CanWhenElementKnownByTypeUsingAction extends AbstractDomainObjectViewWhenElementsKnownByTypeTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewWhenElementKnownUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void whenElementKnown(DomainObjectView<T> subject, Class<S> type, Consumer<? super KnownDomainObject<S>> action) {
			subject.whenElementKnownEx(type, action::accept);
		}
	}

	@Nested
	class CanWhenElementKnownByTypeUsingClosure extends AbstractDomainObjectViewWhenElementsKnownByTypeTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewWhenElementKnownUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void whenElementKnown(DomainObjectView<T> subject, Class<S> type, Consumer<? super KnownDomainObject<S>> action) {
			subject.whenElementKnownEx(type, adaptToClosure(action));
		}
	}
}
