package dev.nokee.model;

import org.junit.jupiter.api.Nested;

public abstract class DomainObjectViewTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureEach extends DomainObjectViewConfigureEachTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanConfigureEachUsingClass extends DomainObjectViewConfigureEachUsingClassTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanConfigureEachUsingSpec extends DomainObjectViewConfigureEachUsingSpecTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanGetElements extends DomainObjectViewElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanFlatMap extends DomainObjectViewFlatMapTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanMap extends DomainObjectViewMapTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanFilter extends DomainObjectViewFilterTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanGet extends DomainObjectViewGetTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanWhenElementKnown extends DomainObjectViewWhenElementKnownTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanWhenElementKnownUsingClass extends DomainObjectViewWhenElementKnownUsingClassTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}
}
