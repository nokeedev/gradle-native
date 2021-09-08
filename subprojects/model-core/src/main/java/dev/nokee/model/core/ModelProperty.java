package dev.nokee.model.core;

import groovy.lang.Closure;
import org.gradle.api.Action;

public interface ModelProperty<T> extends ModelObject<T> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	ModelProperty<T> configure(Action<? super T> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	default ModelProperty<T> configure(@SuppressWarnings("rawtypes") Closure closure) {
		ModelObject.super.configure(closure);
		return this;
	}
}
