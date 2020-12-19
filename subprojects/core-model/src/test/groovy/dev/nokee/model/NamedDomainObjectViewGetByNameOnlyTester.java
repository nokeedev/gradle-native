package dev.nokee.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.val;
import org.junit.jupiter.api.Nested;

public abstract class NamedDomainObjectViewGetByNameOnlyTester<T> {
	protected abstract TestNamedViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanGetByNameOnlyUsingAction extends AbstractNamedDomainObjectViewGetByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameOnlyTester.this.getSubjectGenerator();
		}

		@Override
		protected DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name) {
			return subject.get(name);
		}
	}

	@Nested
	class CanGetByNameOnlyUsingClosure extends AbstractNamedDomainObjectViewGetByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameOnlyTester.this.getSubjectGenerator();
		}

		@Override
		protected DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name) {
			return subject.get(name);
		}
	}

	@Nested
	class CanGetByNameOnlyUsingGroovyDsl extends AbstractNamedDomainObjectViewGetByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewGetByNameOnlyTester.this.getSubjectGenerator();
		}

		@Override
		protected DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name) {
			val sharedData = new Binding();
			val shell = new GroovyShell(sharedData);
			sharedData.setProperty("subject", subject);
			return (DomainObjectProvider<T>) shell.evaluate("subject." + name);
		}
	}
}
