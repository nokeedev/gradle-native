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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.val;
import org.junit.jupiter.api.Nested;

public abstract class NamedDomainObjectViewGetByNameTypedTester<T> {
	protected abstract TestNamedViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanGetByNameOnlyUsingAction extends AbstractNamedDomainObjectViewGetByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name) {
			return subject.get(name, getElementType());
		}
	}

	@Nested
	class CanGetByNameOnlyUsingClosure extends AbstractNamedDomainObjectViewGetByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name) {
			return subject.get(name, getElementType());
		}
	}


	@Nested
	class CanGetByNameTypedUsingAction extends AbstractNamedDomainObjectViewGetByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> DomainObjectProvider<S> get(NamedDomainObjectView<T> subject, String name, Class<S> type) {
			return subject.get(name, type);
		}
	}

	@Nested
	class CanGetByNameTypedUsingClosure extends AbstractNamedDomainObjectViewGetByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> DomainObjectProvider<S> get(NamedDomainObjectView<T> subject, String name, Class<S> type) {
			return subject.get(name, type);
		}
	}

	@Nested
	class CanGetByNameTypedUsingGroovyDsl extends AbstractNamedDomainObjectViewGetByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> DomainObjectProvider<S> get(NamedDomainObjectView<T> subject, String name, Class<S> type) {
			val sharedData = new Binding();
			val shell = new GroovyShell(sharedData);
			sharedData.setProperty("subject", subject);
			sharedData.setProperty("ElementType", type);
			return (DomainObjectProvider<S>) shell.evaluate("subject." + name + "(ElementType)");
		}
	}
}
