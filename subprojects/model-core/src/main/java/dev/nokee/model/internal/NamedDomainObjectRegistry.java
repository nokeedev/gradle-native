package dev.nokee.model.internal;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

interface NamedDomainObjectRegistry {
	<S> NamedDomainObjectProvider<S> register(String name, Class<S> type);
	<S> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action);
	<S> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type);
	<S> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action);

	RegistrableTypes getRegistrableTypes();

	/**
	 * Represent all registrable types for the registry.
	 */
	interface RegistrableTypes extends Iterable<SupportedType> {
		boolean canRegisterType(Class<?> type);
	}

	/**
	 * Describe a supported registrable type for the registry.
	 */
	interface SupportedType {
		boolean supports(Class<?> type);
	}
}
