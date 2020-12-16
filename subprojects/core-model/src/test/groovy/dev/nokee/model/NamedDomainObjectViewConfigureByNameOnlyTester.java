package dev.nokee.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.val;
import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;

public abstract class NamedDomainObjectViewConfigureByNameOnlyTester<T> {
	protected abstract TestNamedViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureByNameOnlyUsingAction extends AbstractNamedDomainObjectViewConfigureByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameOnlyTester.this.getSubjectGenerator();
		}

		@Override
		protected void configure(NamedDomainObjectView<T> subject, String name, Consumer<? super T> action) {
			subject.configure(name, action::accept);
		}
	}

	@Nested
	class CanConfigureByNameOnlyUsingClosure extends AbstractNamedDomainObjectViewConfigureByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameOnlyTester.this.getSubjectGenerator();
		}

		@Override
		protected void configure(NamedDomainObjectView<T> subject, String name, Consumer<? super T> action) {
			subject.configure(name, adaptToClosure(action));
		}
	}

	@Nested
	class CanConfigureByNameOnlyUsingGroovyDsl extends AbstractNamedDomainObjectViewConfigureByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameOnlyTester.this.getSubjectGenerator();
		}

		@Override
		protected void configure(NamedDomainObjectView<T> subject, String name, Consumer<? super T> action) {
			val sharedData = new Binding();
			val shell = new GroovyShell(sharedData);
			sharedData.setProperty("subject", subject);
			sharedData.setProperty("action", action);
			shell.evaluate("subject." + name + " { action.accept(it) }");
		}
	}
}
