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
