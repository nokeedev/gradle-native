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
