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
