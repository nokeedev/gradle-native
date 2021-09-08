package dev.nokee.model.core;

import dev.nokee.model.KnownDomainObject;
import groovy.lang.Closure;
import org.gradle.api.Action;

import java.util.function.Consumer;

public interface ModelObject<T> extends KnownDomainObject<T> {
	/**
	 * Create a new property on the schema of this model object.
	 *
	 * @param identity  the identity of the object represented by the new property, must not be null
	 * @param type  the property type, must not be null
	 * @param <S>  the property type
	 * @return a model property, never null
	 */
	<S> ModelProperty<S> newProperty(Object identity, Class<S> type);

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

	/**
	 * Configures this model object using the specified action.
	 *
	 * @param action  the configuration action, must not be null
	 * @return itself, never null
	 */
	ModelObject<T> configure(Consumer<? super ModelObject<? extends T>> action);
}
