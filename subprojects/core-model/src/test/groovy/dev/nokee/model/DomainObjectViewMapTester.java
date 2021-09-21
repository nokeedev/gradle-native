/*
 * Copyright 2020-2021 the original author or authors.
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
