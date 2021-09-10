package dev.nokee.model.internal;

import org.gradle.api.NamedDomainObjectProvider;

/**
 * Decorates a {@literal NamedDomainObjectProvider}.
 */
interface NamedDomainObjectProviderDecorator {
	<T> NamedDomainObjectProvider<T> decorate(NamedDomainObjectProvider<T> provider);
}
