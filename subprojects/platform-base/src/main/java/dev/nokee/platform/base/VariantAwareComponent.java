/*
 * Copyright 2020 the original author or authors.
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

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * A component with variants.
 *
 * @param <T> type of the component dependencies
 * @since 0.4
 */
public interface VariantAwareComponent<T extends Variant> {
	/**
	 * Configure the variants of this component.
	 *
	 * @return a component variants, never null.
	 */
	View<? extends T> getVariants();

	/**
	 * Configures the component variants using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 */
	void variants(Action<? super View<? extends T>> action);

	/**
	 * Returns the variant dimensions for this component.
	 *
	 * @return variant dimensions, never null
	 */
	VariantDimensions getDimensions();

	/**
	 * Returns the build variants for this component.
	 *
	 * @return a provider of build variants, never null
	 */
	Provider<Set<BuildVariant>> getBuildVariants();
}
