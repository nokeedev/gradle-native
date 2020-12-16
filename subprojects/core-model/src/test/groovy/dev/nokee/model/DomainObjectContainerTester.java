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
