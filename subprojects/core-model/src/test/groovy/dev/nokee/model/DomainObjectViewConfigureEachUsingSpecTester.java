package dev.nokee.model;

import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.alwaysTrue;

public abstract class DomainObjectViewConfigureEachUsingSpecTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureEachElementsUsingAction extends AbstractDomainObjectViewConfigureEachElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingSpecTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Consumer<? super T> action) {
			subject.configureEach(alwaysTrue()::test, action::accept);
		}
	}

	@Nested
	class CanConfigureEachElementsUsingClosure extends AbstractDomainObjectViewConfigureEachElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingSpecTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Consumer<? super T> action) {
			subject.configureEach(alwaysTrue()::test, adaptToClosure(action));
		}
	}

	@Nested
	class CanConfigureEachByTypeUsingAction extends AbstractDomainObjectViewConfigureEachByTypeTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingSpecTester.this.getSubjectGenerator();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected <S extends T> void configureEach(DomainObjectView<T> subject, Class<S> type, Consumer<? super S> action) {
			subject.configureEach(type::isInstance, t -> action.accept((S)t));
		}
	}

	@Nested
	class CanConfigureEachByTypeUsingClosure extends AbstractDomainObjectViewConfigureEachByTypeTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingSpecTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void configureEach(DomainObjectView<T> subject, Class<S> type, Consumer<? super S> action) {
			subject.configureEach(type::isInstance, adaptToClosure(action));
		}
	}

	@Nested
	class CanConfigureEachBySpecUsingAction extends AbstractDomainObjectViewConfigureEachBySpecTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingSpecTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Predicate<? super T> predicate, Consumer<? super T> action) {
			subject.configureEach(predicate::test, action::accept);
		}
	}

	@Nested
	class CanConfigureEachBySpecUsingClosure extends AbstractDomainObjectViewConfigureEachBySpecTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachUsingSpecTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Predicate<? super T> predicate, Consumer<? super T> action) {
			subject.configureEach(predicate::test, adaptToClosure(action));
		}
	}
}
