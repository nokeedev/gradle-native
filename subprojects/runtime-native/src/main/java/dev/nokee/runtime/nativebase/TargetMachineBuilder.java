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

/**
 * A builder for configuring the architecture of a {@link TargetMachine} instances.
 *
 * @since 0.1
 */
public interface TargetMachineBuilder extends TargetMachine, dev.nokee.platform.nativebase.TargetMachineBuilder {
	/**
	 * Creates a {@link TargetMachine} for the operating system of this instance and the x86 32-bit architecture.
	 *
	 * @return a {@link TargetMachine} instance, never null.
	 */
	TargetMachine getX86();

	/**
	 * Creates a {@link TargetMachine} for the operating system of this instance and the x86 64-bit architecture.
	 *
	 * @return a {@link TargetMachine} instance, never null.
	 */
	TargetMachine getX86_64();

	/**
	 * Creates a target machine for the operating system of this instance and with whatever architecture string will be passed.
	 *
	 * @param architectureName  an architecture name, must not be null
	 * @return a {@link TargetMachine} instance, never null
	 * @since 0.5
	 */
	TargetMachine architecture(String architectureName);
}
