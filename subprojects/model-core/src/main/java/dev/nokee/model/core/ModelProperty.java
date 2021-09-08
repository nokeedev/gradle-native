package dev.nokee.model.core;

import groovy.lang.Closure;
import org.gradle.api.Action;

import java.util.function.Consumer;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	ModelProperty<T> configure(Consumer<? super ModelObject<? extends T>> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	<S> ModelProperty<S> as(Class<S> type);
}
