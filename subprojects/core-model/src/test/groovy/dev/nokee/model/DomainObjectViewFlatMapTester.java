package dev.nokee.model;

import com.google.common.collect.ImmutableList;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DomainObjectViewFlatMapTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanProcessElements extends AbstractDomainObjectViewElementProcessTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFlatMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Provider<? extends Iterable<?>> process(DomainObjectView<T> subject, Consumer<? super T> captor) {
			return subject.flatMap(t -> {
				captor.accept(t);
				return ImmutableList.of(t);
			});
		}
	}

	@Nested
	class CanProvideElements extends AbstractDomainObjectViewElementProviderTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFlatMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Provider<? extends Iterable<T>> provider(DomainObjectView<T> subject) {
			return subject.flatMap(ImmutableList::of);
		}
	}

	@Nested
	class CanTransformElements extends AbstractDomainObjectViewElementTransformTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFlatMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<?> transform(DomainObjectView<T> subject, Function<T, ?> mapper) {
			return subject.flatMap(t -> ImmutableList.of(mapper.apply(t))).get();
		}
	}

	@Nested
	class CanFilterElements extends AbstractDomainObjectViewElementFilterTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFlatMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<?> filter(DomainObjectView<T> subject, Predicate<T> predicate) {
			return subject.flatMap(t -> {
				if (predicate.test(t)) {
					return ImmutableList.of(t);
				}
				return ImmutableList.of();
			}).get();
		}
	}

	@Nested
	class CanQueryElements extends AbstractDomainObjectViewElementQueryTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewFlatMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<? extends T> query(DomainObjectView<T> subject) {
			return subject.flatMap(ImmutableList::of).get();
		}
	}
}
