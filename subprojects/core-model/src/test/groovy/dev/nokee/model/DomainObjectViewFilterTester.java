package dev.nokee.model;

import dev.nokee.utils.SpecUtils;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class DomainObjectViewFilterTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanProcessElements extends AbstractDomainObjectViewElementProcessTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFilterTester.this.getSubjectGenerator();
		}

		@Override
		protected Provider<? extends Iterable<?>> process(DomainObjectView<T> subject, Consumer<? super T> captor) {
			return subject.filter(t -> {
				captor.accept(t);
				return true;
			});
		}
	}

	@Nested
	class CanProvideElements extends AbstractDomainObjectViewElementProviderTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFilterTester.this.getSubjectGenerator();
		}

		@Override
		protected Provider<? extends Iterable<T>> provider(DomainObjectView<T> subject) {
			return subject.filter(SpecUtils.satisfyAll());
		}
	}

	@Nested
	class CanFilterElements extends AbstractDomainObjectViewElementFilterTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFilterTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<?> filter(DomainObjectView<T> subject, Predicate<T> predicate) {
			return subject.filter(predicate::test).get();
		}
	}

	@Nested
	class CanQueryElements extends AbstractDomainObjectViewElementQueryTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFilterTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<? extends T> query(DomainObjectView<T> subject) {
			return subject.filter(SpecUtils.satisfyAll()).get();
		}
	}
}
