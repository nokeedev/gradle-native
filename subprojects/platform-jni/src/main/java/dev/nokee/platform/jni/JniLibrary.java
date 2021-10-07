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
package dev.nokee.platform.jni;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.runtime.nativebase.TargetMachine;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.util.ConfigureUtil;

/**
 * Configuration for a specific Java Native Interface (JNI) library variant, defining the dependencies that make up the library plus other settings.
 *
 * @since 0.2
 */
public interface JniLibrary extends Variant, DependencyAwareComponent<JavaNativeInterfaceNativeComponentDependencies>, BinaryAwareComponent {
	/**
	 * Specifies the resource path where the native components of the JNI library will be located within the JAR.
	 *
	 * @return a property for configuring the resource path, never null.
	 */
	Property<String> getResourcePath();

	/**
	 * Returns the target machine for this variant.
	 *
	 * @return a {@link TargetMachine} instance, never null.
	 */
	TargetMachine getTargetMachine();

	/**
	 * Returns the shared library binary built for this variant.
	 *
	 * @return a {@link SharedLibraryBinary} instance, never null.
	 * @since 0.3
	 */
	SharedLibraryBinary getSharedLibrary();

	/**
	 * Configure the shared library binary for this variant.
	 *
	 * @param action configuration action for {@link SharedLibraryBinary}.
	 * @since 0.3
	 */
	void sharedLibrary(Action<? super SharedLibraryBinary> action);

	/**
	 * Configure the shared library binary for this variant.
	 *
	 * @param closure configuration closure for {@link SharedLibraryBinary}.
	 * @since 0.3
	 */
	default void sharedLibrary(@DelegatesTo(value = SharedLibraryBinary.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		sharedLibrary(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Configure the native runtime files to include inside the JNI JAR at the resource path location.
	 *
	 * By default, the native runtime files defaults contains the linked shared library and all of the native runtime dependencies.
	 *
	 * @return a property for configuring the native runtime files to include inside the JNI JAR, never null.
	 * @see #getResourcePath() for the resource path location for the native runtime files
	 * @since 0.4
	 */
	ConfigurableFileCollection getNativeRuntimeFiles();
}
