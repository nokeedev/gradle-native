package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.DomainObjectElementConfigurer;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.specs.Spec;

public final class DefaultDomainObjectElementConfigurer<T> implements DomainObjectElementConfigurer<T> {

	private final DomainObjectCollection<DomainObjectElement<T>> delegate;
	private final DomainObjectElementFilter<T> filter;

	public DefaultDomainObjectElementConfigurer(org.gradle.api.DomainObjectCollection<DomainObjectElement<T>> delegate, DomainObjectElementFilter<T> filter) {
		this.delegate = delegate;
		this.filter = filter;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(filter.filter(it -> action.execute(it.get())));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(filter.withType(type).filter(it -> {
			action.execute(type.cast(it.get()));
		}));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Spec<? super S> spec, Action<? super S> action) {
		delegate.configureEach(it -> {
			if (type.isAssignableFrom(it.getType())) {
				if (spec.isSatisfiedBy(type.cast(it.get()))) {
					action.execute(type.cast(it.get()));
				}
			}
		});
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(it -> {
			if (spec.isSatisfiedBy(it.get())) {
				action.execute(it.get());
			}
		});
	}
}
