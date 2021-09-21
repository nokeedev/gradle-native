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

public abstract class DomainObjectContainerTester<T> extends DomainObjectViewTester<T> {
	protected abstract TestContainerGenerator<T> getSubjectGenerator();

	// TODO: Test Groovy DSL simplification
	//   - register using container.<name> -> if default allowed... mostly not for now
	//   - register using container.<name> { } -> if default allowed... mostly not for now
	//   - register using container.<name>(Type) -> same as register(name, Type)
	//   - register using container.<name>(Type) {} -> same as register(name, Type, action)

	@Nested
	class CanRegister extends AbstractDomainObjectContainerRegisterTester<T> {
		@Override
		protected DomainObjectProvider<T> register(DomainObjectContainer<T> subject, String name, Class<T> type) {
			return subject.register(name, type);
		}

		@Override
		protected TestContainerGenerator<T> getSubjectGenerator() {
			return DomainObjectContainerTester.this.getSubjectGenerator();
		}
	}
}
