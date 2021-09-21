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
package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.core.CoordinateAxis;

/**
 * Represents a combination of operating system and cpu architecture that a variant might be built for.
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.
 * Use {@link TargetMachineFactory} to create an instance.
 * </p>
 *
 * @since 0.1
 */
public interface TargetMachine extends dev.nokee.platform.nativebase.TargetMachine {
	/**
	 * The target machine coordinate axis for variant calculation.
	 * @since 0.5
	 */
	CoordinateAxis<TargetMachine> TARGET_MACHINE_COORDINATE_AXIS = CoordinateAxis.of(TargetMachine.class, "target-machine");

	/**
	 * Returns the target operating system family.
	 *
	 * @return a {@link OperatingSystemFamily} instance, never null.
	 */
	OperatingSystemFamily getOperatingSystemFamily();

	/**
	 * Returns the target architecture.
	 *
	 * @return a {@link MachineArchitecture} instance, never null.
	 */
	MachineArchitecture getArchitecture();
}
