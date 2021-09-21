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
 * A factory for creating {@link TargetMachine} instances.
 *
 * @since 0.1
 */
public interface TargetMachineFactory extends dev.nokee.platform.nativebase.TargetMachineFactory {
	/**
	 * Creates a {@link TargetMachineBuilder} for the Windows operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null
	 */
	TargetMachineBuilder getWindows();

	/**
	 * Creates a {@link TargetMachineBuilder} for the Linux operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null
	 */
	TargetMachineBuilder getLinux();

	/**
	 * Creates a {@link TargetMachineBuilder} for the macOS operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null
	 */
	TargetMachineBuilder getMacOS();

	/**
	 * Creates a {@link TargetMachineBuilder} for the FreeBSD operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null
	 */
	TargetMachineBuilder getFreeBSD();

	/**
	 * Creates a target machine with whatever operating system string will be passed.
	 *
	 * @param osFamilyName  a operating system family name, must not be null
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null
	 * @since 0.5
	 */
	TargetMachineBuilder os(String osFamilyName);
}
