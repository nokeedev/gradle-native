package dev.nokee.model.internal.core;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.registry.ManagedModelProjection;
import dev.nokee.model.internal.registry.SupplyingModelProjection;
import dev.nokee.model.internal.type.ModelType;

import java.util.function.Function;

import static dev.nokee.internal.Factories.*;
import static dev.nokee.model.internal.type.ModelType.typeOf;

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
		return createdUsing(typeOf(instance), constant(instance));
	}

	/**
	 * Creates a managed model projection of the specified type.
	 * A managed projection will be bind to the instantiator of the model registry on model registration.
	 * A managed instance is created, memoized and decorated on first use.
	 *
	 * @param type  the managed type
	 * @param parameters  the parameters to use when constructing the managed type
	 * @param <T>  the type of the projection
	 * @return a model projection of the specified type, never null.
	 * @see ModelNodeContext#injectCurrentModelNodeIfAllowed(Object) for decoration information
	 * @see dev.nokee.internal.reflect.Instantiator
	 */
	public static <T> ModelProjection managed(ModelType<T> type, Object... parameters) {
		return new ManagedModelProjection<>(type, parameters);
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
		return new SupplyingModelProjection<>(type, asSupplier(memoize(compose(factory, InjectCurrentNodeFunction.INSTANCE.withNarrowType()))));
	}

	// To ensure Objects.equals works with the created model projections
	private enum InjectCurrentNodeFunction implements Function<Object, Object> {
		INSTANCE;

		@Override
		public Object apply(Object o) {
			return ModelNodeContext.injectCurrentModelNodeIfAllowed(o);
		}

		@SuppressWarnings("unchecked")
		public <T, R> Function<T, R> withNarrowType() {
			return (Function<T, R>) this;
		}

		@Override
		public String toString() {
			return "injectCurrentModelNode()";
		}
	}
}
