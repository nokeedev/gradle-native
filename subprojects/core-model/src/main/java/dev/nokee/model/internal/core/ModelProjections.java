package dev.nokee.model.internal.core;

import dev.nokee.internal.Factories;
import dev.nokee.internal.Factory;
import dev.nokee.model.internal.registry.ManagedModelProjection;
import dev.nokee.model.internal.registry.SupplyingModelProjection;
import dev.nokee.model.internal.type.ModelType;

import static dev.nokee.internal.Factories.compose;
import static dev.nokee.internal.Factories.memoize;

public final class ModelProjections {
	private ModelProjections() {}

	/**
	 * Creates model projection of specified instance.
	 * The instance will be decorated with the associated model node on first invocation.
	 *
	 * @param instance  the instance to project
	 * @param <T>  the type of the projection
	 * @return a model projection of the specified instance, never null.
	 * @see ModelNodeContext#injectCurrentModelNodeIfAllowed(Object) for decoration information
	 */
	public static <T> ModelProjection ofInstance(T instance) {
		return createdUsing(ModelType.typeOf(instance), Factories.constant(instance));
	}

	/**
	 * Creates a managed model projection of the specified type.
	 * A managed projection will be bind to the instantiator of the model registry on model registration.
	 * A managed instance is created, memoized and decorated on first use.
	 *
	 * @param type  the managed type
	 * @param <T>  the type of the projection
	 * @return a model projection of the specified type, never null.
	 * @see ModelNodeContext#injectCurrentModelNodeIfAllowed(Object) for decoration information
	 */
	public static <T> ModelProjection managed(ModelType<T> type) {
		return ManagedModelProjection.of(type);
	}

	/**
	 * Creates a model projection created by the specified factory.
	 * The instance is memoized and decorated on first use.
	 *
	 * @param type  the created type
	 * @param factory  the factory used to create the instance
	 * @param <T>  the type of the projection
	 * @return a model projection of the specified type, never null.
	 * @see ModelNodeContext#injectCurrentModelNodeIfAllowed(Object) for decoration information
	 */
	public static <T> ModelProjection createdUsing(ModelType<T> type, Factory<T> factory) {
		return new SupplyingModelProjection<>(type, memoize(compose(factory, ModelNodeContext::injectCurrentModelNodeIfAllowed)));
	}
}
