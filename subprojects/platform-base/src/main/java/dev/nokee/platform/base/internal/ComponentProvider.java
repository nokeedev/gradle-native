package dev.nokee.platform.base.internal;

import org.gradle.api.NamedDomainObjectProvider;

import javax.inject.Inject;

public class ComponentProvider<T extends Component> {
	private final NamedDomainObjectProvider<T> delegate;

	@Inject
	public ComponentProvider(NamedDomainObjectProvider<T> delegate) {
		this.delegate = delegate;
	}

	public T get() {
		return delegate.get();
	}
}
