package dev.nokee.model;

public abstract class AbstractDomainObjectContainerTester<T> extends AbstractNamedDomainObjectViewTester<T> {
	protected abstract TestContainerGenerator<T> getSubjectGenerator();

	protected DomainObjectContainer<T> createSubject() {
		return (DomainObjectContainer<T>) super.createSubject();
	}

	protected DomainObjectContainer<T> createSubject(String name) {
		return (DomainObjectContainer<T>) super.createSubject(name);
	}
}
