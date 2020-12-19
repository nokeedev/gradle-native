package dev.nokee.model;

public abstract class DomainObjectViewGetTester<T> extends AbstractDomainObjectViewElementQueryTester<T> {
	@Override
	protected Iterable<? extends T> query(DomainObjectView<T> subject) {
		return subject.get();
	}
}
