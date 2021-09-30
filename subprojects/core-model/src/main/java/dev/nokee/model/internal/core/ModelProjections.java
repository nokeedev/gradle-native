/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public static <T> TypeCompatibilityModelProjectionSupport<T> ofInstance(T instance) {
		return new InstanceModelProjection<>(typeOf(instance), instance);
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
	public static <T> TypeCompatibilityModelProjectionSupport<T> managed(ModelType<T> type, Object... parameters) {
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
	public static <T> TypeCompatibilityModelProjectionSupport<T> createdUsing(ModelType<T> type, Factory<T> factory) {
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
