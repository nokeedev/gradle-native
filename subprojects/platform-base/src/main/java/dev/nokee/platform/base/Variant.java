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

import org.gradle.api.Named;
import org.gradle.api.provider.Provider;

/**
 * A variant realization of a component.
 *
 * @since 0.2
 */
public interface Variant extends Named, BinaryAwareComponent {
	/**
	 * Configure the binaries of this variant.
	 * The view contains only the binaries participating to this variant.
	 *
	 * @return a {@link BinaryView} for configuring each binary, never null.
	 * @since 0.4
	 */
	BinaryView<Binary> getBinaries();

	/**
	 * Returns a the development binary for this variant.
	 * The development binary is used by lifecycle tasks.
	 *
	 * @return a provider for the development binary, never null.
	 * @since 0.4
	 */
	Provider<Binary> getDevelopmentBinary();

	/**
	 * Returns the build variant information of this variant.
	 *
	 * @return a {@link BuildVariant} instance representing this variant axis value across all the dimension, never null.
	 * @since 0.5
	 */
	BuildVariant getBuildVariant();
}
