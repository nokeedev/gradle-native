package dev.nokee.model;

public abstract class AbstractNamedDomainObjectViewTester<T> extends AbstractDomainObjectViewTester<T> {
	protected abstract TestNamedViewGenerator<T> getSubjectGenerator();

	protected final boolean isTestingContainerType() {
		return DomainObjectContainer.class.isAssignableFrom(getViewUnderTestType());
	}

	protected NamedDomainObjectView<T> createSubject() {
		return (NamedDomainObjectView<T>) super.createSubject();
	}

	protected NamedDomainObjectView<T> createSubject(String name) {
		return (NamedDomainObjectView<T>) super.createSubject(name);
	}
}
