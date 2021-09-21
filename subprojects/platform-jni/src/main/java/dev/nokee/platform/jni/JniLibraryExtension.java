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
package dev.nokee.platform.jni;

import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.TargetMachineFactory;
import org.gradle.api.provider.SetProperty;

/**
 * Configuration for a Java Native Interface (JNI) library, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the JNI Library Plugin.</p>
 *
 * @since 0.1
 */
@Deprecated // Use JavaNativeInterfaceLibrary instead.
public interface JniLibraryExtension extends DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, VariantAwareComponent<JniLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, SourceAwareComponent<JavaNativeInterfaceLibrarySources> {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link JavaNativeInterfaceLibraryComponentDependencies}, never null.
	 * @since 0.1
	 */
	JavaNativeInterfaceLibraryComponentDependencies getDependencies();

	/**
	 * Specifies the target machines this component should be built for.
	 * The "machines" extension property (see {@link TargetMachineFactory}) can be used to construct common operating system and architecture combinations.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetMachines = [machines.linux.x86_64, machines.windows.x86_64]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetMachine}, never null.
	 * @since 0.1
	 */
	SetProperty<TargetMachine> getTargetMachines();
}
