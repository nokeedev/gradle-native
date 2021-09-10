package dev.nokee.model.internal;

import org.gradle.api.NamedDomainObjectContainer;

/**
 * Creates {@literal NamedDomainObjectProvider} self-mutating decorator for container.
 */
interface NamedDomainObjectProviderSelfMutationDecoratorFactory {
	NamedDomainObjectProviderDecorator forContainer(NamedDomainObjectContainer<?> container);
}
