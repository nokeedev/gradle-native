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
