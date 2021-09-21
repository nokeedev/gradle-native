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
package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.TargetMachineFactory;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import org.gradle.api.provider.SetProperty;

/**
 * Represents a component that targets multiple target machines.
 * @since 0.4
 */
public interface TargetMachineAwareComponent {
	/**
	 * Specifies the target machines this component should be built for.
	 * The {@link #getMachines()} property (see {@link TargetMachineFactory}) can be used to construct common operating system and architecture combinations.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetMachines = [machines.linux.x86_64, machines.windows.x86_64]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetMachine}, never null.
	 */
	SetProperty<TargetMachine> getTargetMachines();

	/**
	 * Returns a factory to create target machines when configuring {@link #getTargetMachines()}.
	 *
	 * @return a {@link TargetMachineFactory} for creating {@link TargetMachine} instance, never null.
	 */
	default TargetMachineFactory getMachines() {
		return NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY;
	}
}
