/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.nativebase.internal;

import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.host;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.isTargetingHost;
import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.*;

class TargetMachines_IsTargetingHostTest {
	@Test
	void returnsTrueForHostInstance() {
		assertTrue(isTargetingHost(host()));
	}

	@Test
	void returnsTrueForRenamedHostInstance() {
		assertTrue(isTargetingHost(host().named("foo")));
	}

	@Test
	void returnsTrueForEquivalentHostTargetMachine() {
		assertAll(
			() -> assertTrue(isTargetingHost(TARGET_MACHINE_FACTORY.os(getProperty("os.name")))),
			() -> assertTrue(isTargetingHost(TARGET_MACHINE_FACTORY.os(getProperty("os.name")).architecture(getProperty("os.arch"))))
		);
	}

	@Test
	void returnsFalseForNonHostTargetMachine() {
		assertAll(
			() -> assertFalse(isTargetingHost(TARGET_MACHINE_FACTORY.os("non-host"))),
			() -> assertFalse(isTargetingHost(TARGET_MACHINE_FACTORY.os("non-host").architecture("non-host"))),
			() -> assertFalse(isTargetingHost(TARGET_MACHINE_FACTORY.os(getProperty("os.name")).architecture("non-host")))
		);
	}
}
