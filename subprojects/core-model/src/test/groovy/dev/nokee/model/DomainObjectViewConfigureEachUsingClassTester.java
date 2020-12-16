package dev.nokee.model;

import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;

public abstract class DomainObjectViewConfigureEachUsingClassTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureEachElementsUsingAction extends AbstractDomainObjectViewConfigureEachElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Consumer<? super T> action) {
			subject.configureEach(getElementType(), action::accept);
		}
	}

	@Nested
	class CanConfigureEachElementsUsingClosure extends AbstractDomainObjectViewConfigureEachElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Consumer<? super T> action) {
			subject.configureEach(getElementType(), adaptToClosure(action));
		}
	}

	@Nested
	class CanConfigureEachByTypeUsingAction extends AbstractDomainObjectViewConfigureEachByTypeTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void configureEach(DomainObjectView<T> subject, Class<S> type, Consumer<? super S> action) {
			subject.configureEach(type, action::accept);
		}
	}

	@Nested
	class CanConfigureEachByTypeUsingClosure extends AbstractDomainObjectViewConfigureEachByTypeTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingClassTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void configureEach(DomainObjectView<T> subject, Class<S> type, Consumer<? super S> action) {
			subject.configureEach(type, adaptToClosure(action));
		}
	}
}
