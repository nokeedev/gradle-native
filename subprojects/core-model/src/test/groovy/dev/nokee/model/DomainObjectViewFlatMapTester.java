/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
