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
package dev.nokee.platform.nativebase;

/**
 * Represents a target architecture of a configuration.
 * Typical architectures include "x86" and "x86-64".
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.</p>
 *
 * @since 0.1
 */
@Deprecated
public interface MachineArchitecture {
	/**
	 * Returns whether or not the architecture has 32-bit pointer size.
	 *
	 * @return {@code true} if the architecture is 32-bit or {@code false} otherwise.
	 * @since 0.2
	 */
	boolean is32Bit();

	/**
	 * Returns whether or not the architecture has 64-bit pointer size.
	 *
	 * @return {@code true} if the architecture is 64-bit or {@code false} otherwise.
	 * @since 0.2
	 */
	boolean is64Bit();
}
