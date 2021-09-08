package dev.nokee.model.core;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.streams.ModelStream;
import groovy.lang.Closure;
import org.gradle.api.Action;

import java.util.Optional;
import java.util.function.Consumer;

public interface ModelObject<T> extends KnownDomainObject<T> {
	/**
	 * Returns the parent object of this object if available.
	 *
	 * @return the parent object for this object, never null
	 */
	Optional<ModelObject<?>> getParent();

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
	 * Return a property from the schema of this model object.
	 *
	 * @param name  the property name, must not be null
	 * @param type  the property type, must not be null
	 * @param <S>  the property type
	 * @return a property from the schema of this model object, never null
	 */
	<S> ModelProperty<S> property(String name, Class<S> type);

	/**
	 * Returns a stream of all the property of this object.
	 *
	 * @return the properties stream, never null
	 */
	ModelStream<ModelProperty<?>> getProperties();

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
