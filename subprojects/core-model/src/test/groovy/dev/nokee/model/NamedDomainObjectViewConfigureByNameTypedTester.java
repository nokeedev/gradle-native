package dev.nokee.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.val;
import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;

public abstract class NamedDomainObjectViewConfigureByNameTypedTester<T> {
	protected abstract TestNamedViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureByNameOnlyUsingAction extends AbstractNamedDomainObjectViewConfigureByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected void configure(NamedDomainObjectView<T> subject, String name, Consumer<? super T> action) {
			subject.configure(name, getElementType(), action::accept);
		}
	}

	@Nested
	class CanConfigureByNameOnlyUsingClosure extends AbstractNamedDomainObjectViewConfigureByNameOnlyTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected void configure(NamedDomainObjectView<T> subject, String name, Consumer<? super T> action) {
			subject.configure(name, getElementType(), adaptToClosure(action));
		}
	}

	@Nested
	class CanConfigureByNameTypedUsingAction extends AbstractNamedDomainObjectViewConfigureByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void configure(NamedDomainObjectView<T> subject, String name, Class<S> type, Consumer<? super S> action) {
			subject.configure(name, type, action::accept);
		}
	}

	@Nested
	class CanConfigureByNameTypedUsingClosure extends AbstractNamedDomainObjectViewConfigureByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void configure(NamedDomainObjectView<T> subject, String name, Class<S> type, Consumer<? super S> action) {
			subject.configure(name, type, adaptToClosure(action));
		}
	}

	@Nested
	class CanConfigureByNameTypedUsingGroovyDsl extends AbstractNamedDomainObjectViewConfigureByNameTypedTester<T> {
		@Override
		protected TestNamedViewGenerator<T> getSubjectGenerator() {
			return NamedDomainObjectViewConfigureByNameTypedTester.this.getSubjectGenerator();
		}

		@Override
		protected <S extends T> void configure(NamedDomainObjectView<T> subject, String name, Class<S> type, Consumer<? super S> action) {
			val sharedData = new Binding();
			val shell = new GroovyShell(sharedData);
			sharedData.setProperty("subject", subject);
			sharedData.setProperty("action", action);
			sharedData.setProperty("ElementType", type);
			shell.evaluate("subject." + name + "(ElementType) { action.accept(it) }");
		}
	}
}
