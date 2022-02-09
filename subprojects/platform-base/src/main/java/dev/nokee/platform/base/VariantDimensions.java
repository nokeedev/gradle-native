/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;
import org.gradle.api.provider.SetProperty;

/**
 * The dimensions of a component for build variant.
 *
 * Creates new variants by declaring additional dimensions:
 * <pre class="autoTested">
 * enum MyAxis { v0, v1, v2 }
 * library {
 *     // We recommend keeping a reference of the value property as an extension
 *     extensions.add('myAxis', variantDimensions.newAxis(MyAxis))
 *     myAxis.set([v0, v2])  // configure the values
 * }
 * assert library.buildVariants.get().any { it.hasAxisOf(MyAxis.v0) }
 * assert !library.buildVariants.get().any { it.hasAxisOf(MyAxis.v1) }
 * </pre>
 *
 * @since 0.5
 */
public interface VariantDimensions {
	/**
	 * Creates a new dimension axis of the specified type.
	 *
	 * @param axisType  the dimension axis type, must not be null
	 * @param <T>  the dimension axis type
	 * @return a set property to configure the axis values, never null
	 */
	<T> SetProperty<T> newAxis(Class<T> axisType);

	/**
	 * Creates a new dimension axis of the specified type and additional configuration.
	 *
	 * @param axisType  the dimension axis type, must not be null
	 * @param action  the dimension builder configuration
	 * @param <T>  the dimension axis type
	 * @return a set property to configure the axis values, never null
	 */
	<T> SetProperty<T> newAxis(Class<T> axisType, Action<? super VariantDimensionBuilder<T>> action);
	<T> SetProperty<T> newAxis(Class<T> axisType, @ClosureParams(value = SimpleType.class, options = "dev.nokee.platform.base.VariantDimensionBuilder") @DelegatesTo(value = VariantDimensionBuilder.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);
}
