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

import dev.nokee.model.testers.*;
import org.junit.jupiter.api.Nested;

public abstract class DomainObjectProviderTest<T> {
	protected abstract TestProviderGenerator<T> getSubjectGenerator();

	@Nested
	class ConfigureUsingAction extends DomainObjectProviderConfigureUsingActionTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class ConfigureUsingClosure extends DomainObjectProviderConfigureUsingClosureTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class GetType extends DomainObjectProviderGetTypeTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class GetIdentifier extends DomainObjectProviderGetIdentifierTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class Map extends DomainObjectProviderMapTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class FlatMap extends DomainObjectProviderFlatMapTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class Equals extends DomainObjectProviderEqualsTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}

	@Nested
	class Get extends DomainObjectProviderGetTester<T> {
		@Override
		public TestProviderGenerator<T> getSubjectGenerator() {
			return DomainObjectProviderTest.this.getSubjectGenerator();
		}
	}
}
