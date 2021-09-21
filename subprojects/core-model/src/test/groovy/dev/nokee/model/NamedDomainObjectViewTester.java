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

public abstract class NamedDomainObjectViewTester<T> extends DomainObjectViewTester<T> {
	protected abstract TestNamedViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureByNameOnly extends NamedDomainObjectViewConfigureByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanConfigureByNameTyped extends NamedDomainObjectViewConfigureByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanGetByNameOnly extends NamedDomainObjectViewGetByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanGetByNameTyped extends NamedDomainObjectViewGetByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewTester.this.getSubjectGenerator();
		}
	}
}
