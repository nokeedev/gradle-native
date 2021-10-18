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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

/**
 * A component with binaries.
 *
 * @since 0.4
 */
public interface BinaryAwareComponent {
	/**
	 * Returns the binaries for this component.
	 *
	 * @return a {@link BinaryView} for configuring each binary, never null.
	 */
	BinaryView<Binary> getBinaries();

	/**
	 * Configure the binaries of this component.
	 *
	 * @param action configuration action for {@link ComponentBinaries}.
	 */
	void binaries(Action<? super BinaryView<Binary>> action);

	/** @see #binaries(Action) */
	void binaries(@DelegatesTo(value = BinaryView.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);
}
