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
