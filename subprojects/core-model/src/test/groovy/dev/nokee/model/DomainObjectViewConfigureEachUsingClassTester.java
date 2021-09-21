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
