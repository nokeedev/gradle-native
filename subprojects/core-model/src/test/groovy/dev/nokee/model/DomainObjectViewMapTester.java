package dev.nokee.model;

import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;
import java.util.function.Function;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public abstract class DomainObjectViewMapTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanProcessElements extends AbstractDomainObjectViewElementProcessTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Provider<? extends Iterable<?>> process(DomainObjectView<T> subject, Consumer<? super T> captor) {
			return subject.map(t -> {
				captor.accept(t);
				return t;
			});
		}
	}

	@Nested
	class CanProvideElements extends AbstractDomainObjectViewElementProviderTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Provider<? extends Iterable<T>> provider(DomainObjectView<T> subject) {
			return subject.map(noOpTransformer());
		}
	}

	@Nested
	class CanTransformElements extends AbstractDomainObjectViewElementTransformTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<?> transform(DomainObjectView<T> subject, Function<T, ?> mapper) {
			return subject.map(mapper::apply).get();
		}
	}

	@Nested
	class CanQueryElements extends AbstractDomainObjectViewElementQueryTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewMapTester.this.getSubjectGenerator();
		}

		@Override
		protected Iterable<? extends T> query(DomainObjectView<T> subject) {
			return subject.map(noOpTransformer()).get();
		}
	}}
