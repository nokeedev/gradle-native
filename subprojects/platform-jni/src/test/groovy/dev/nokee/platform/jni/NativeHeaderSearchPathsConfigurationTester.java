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
package dev.nokee.platform.jni;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.cpp.CppBinary;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;

public interface NativeHeaderSearchPathsConfigurationTester {
	Configuration subject();

	String displayName();

	String implementationConfigurationName();
	String compileOnlyConfigurationName();

	@Test
	default void extendsFromNativeCompileOnlyConfiguration() {
		assertThat(subject(), extendsFrom(hasItem(named(compileOnlyConfigurationName()))));
	}

	@Test
	default void extendsFromNativeImplementationConfiguration() {
		assertThat(subject(), extendsFrom(hasItem(named(implementationConfigurationName()))));
	}

	@Test
	default void hasOperatingSystemFamilyAttribute() {
		assertThat(subject(), attributes(hasKey(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
		assertThat(subject(), attributes(hasKey(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
	}

	@Test
	default void hasMachineArchitectureAttribute() {
		assertThat(subject(), attributes(hasKey(MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
		assertThat(subject(), attributes(hasKey(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
	}

	@Test
	default void hasLegacyBuildTypeAttributes() {
		assertThat(subject(), attributes(hasKey(CppBinary.DEBUGGABLE_ATTRIBUTE)));
		assertThat(subject(), attributes(hasKey(CppBinary.OPTIMIZED_ATTRIBUTE)));
	}

	@Test
	default void hasDescription() {
		assertThat(subject(), description("Header search paths for " + displayName() + "."));
	}
}
