package dev.nokee.model.core;

import dev.nokee.model.KnownDomainObject;
import groovy.lang.Closure;
import org.gradle.api.Action;

public interface ModelObject<T> extends KnownDomainObject<T> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	ModelObject<T> configure(Action<? super T> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	default ModelObject<T> configure(@SuppressWarnings("rawtypes") Closure closure) {
		KnownDomainObject.super.configure(closure);
		return this;
	}
}
