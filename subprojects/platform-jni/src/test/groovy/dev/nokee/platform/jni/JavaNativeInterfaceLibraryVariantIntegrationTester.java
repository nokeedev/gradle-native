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

import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.NativeBinary;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class JavaNativeInterfaceLibraryVariantIntegrationTester {
	public abstract JniLibrary subject();

	public abstract Project project();

	public abstract String displayName();

	public abstract String variantName();

	@BeforeEach
	void realizeVariant() {
		subject();
	}

	@Nested
	class ComponentDependenciesTest extends JavaNativeInterfaceNativeComponentDependenciesIntegrationTester {
		public JavaNativeInterfaceNativeComponentDependencies subject() {
			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getDependencies();
		}

		@Override
		public String variantName() {
			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName();
		}
	}

	@Nested
	class NativeImplementationConfigurationTest {
		public Configuration subject() {
			project().getConfigurations().forEach(System.out::println);
			return project().getConfigurations().getByName(variantName() + "NativeImplementation");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native implementation dependencies for " + displayName() + "."));
		}
	}

	@Nested
	class NativeCompileOnlyConfigurationTest {
		@Test
		void noNativeCompileOnlyConfigurationWhenNoNativeLanguageApplied() {
			assertThat(project().getConfigurations(), not(hasItem(named(variantName() + "NativeCompileOnly"))));
		}

		abstract class NativeCompileOnlyConfigurationTester {
			public Configuration subject() {
				return project().getConfigurations().getByName(variantName() + "NativeCompileOnly");
			}

			@Test
			void isDeclarable() {
				assertThat(subject(), ConfigurationMatchers.declarable());
			}

			@Test
			void hasDescription() {
				assertThat(subject(), ConfigurationMatchers.description("Native compile only dependencies for " + displayName() + "."));
			}
		}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.c-language")
		class WhenCLanguagePluginApplied extends NativeCompileOnlyConfigurationTester {}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.cpp-language")
		class WhenCppLanguagePluginApplied extends NativeCompileOnlyConfigurationTester {}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
		class WhenObjectiveCLanguagePluginApplied extends NativeCompileOnlyConfigurationTester {}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
		class WhenObjectiveCppLanguagePluginApplied extends NativeCompileOnlyConfigurationTester {}
	}

	@Nested
	class NativeLinkOnlyConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName(variantName() + "NativeLinkOnly");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native link only dependencies for " + displayName() + "."));
		}
	}

	@Nested
	class NativeRuntimeOnlyConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName(variantName() + "NativeRuntimeOnly");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native runtime only dependencies for " + displayName() + "."));
		}
	}

	@Nested
	class NativeLinkLibrariesConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName(variantName() + "NativeLinkLibraries");
		}

		@Test
		void isResolvable() {
			assertThat(subject(), ConfigurationMatchers.resolvable());
		}

		@Test
		void hasNativeLinkUsage() {
			assertThat(subject(), attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("native-link"))));
		}

		@Test
		void extendsFromNativeImplementationConfiguration() {
			assertThat(subject(), extendsFrom(hasItem(named(variantName() + "NativeImplementation"))));
		}

		@Test
		void extendsFromNativeLinkOnlyConfiguration() {
			assertThat(subject(), extendsFrom(hasItem(named(variantName() + "NativeLinkOnly"))));
		}

		@Test
		void hasOperatingSystemFamilyAttribute() {
			assertThat(subject(), attributes(hasKey(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
			assertThat(subject(), attributes(hasKey(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
		}

		@Test
		void hasMachineArchitectureAttribute() {
			assertThat(subject(), attributes(hasKey(MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
			assertThat(subject(), attributes(hasKey(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
		}

		@Test
		void hasLegacyBuildTypeAttributes() {
			assertThat(subject(), attributes(hasKey(CppBinary.DEBUGGABLE_ATTRIBUTE)));
			assertThat(subject(), attributes(hasKey(CppBinary.OPTIMIZED_ATTRIBUTE)));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native link libraries for " + displayName() + "."));
		}
	}

	@Nested
	class NativeRuntimeLibrariesConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName(variantName() + "NativeRuntimeLibraries");
		}

		@Test
		void isResolvable() {
			assertThat(subject(), ConfigurationMatchers.resolvable());
		}

		@Test
		void hasNativeRuntimeUsage() {
			assertThat(subject(), attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("native-runtime"))));
		}

		@Test
		void extendsFromNativeImplementationConfiguration() {
			assertThat(subject(), extendsFrom(hasItem(named(variantName() + "NativeImplementation"))));
		}

		@Test
		void extendsFromNativeRuntimeOnlyConfiguration() {
			assertThat(subject(), extendsFrom(hasItem(named(variantName() + "NativeRuntimeOnly"))));
		}

		@Test
		void hasOperatingSystemFamilyAttribute() {
			assertThat(subject(), attributes(hasKey(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
			assertThat(subject(), attributes(hasKey(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
		}

		@Test
		void hasMachineArchitectureAttribute() {
			assertThat(subject(), attributes(hasKey(MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
			assertThat(subject(), attributes(hasKey(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
		}

		@Test
		void hasLegacyBuildTypeAttributes() {
			assertThat(subject(), attributes(hasKey(CppBinary.DEBUGGABLE_ATTRIBUTE)));
			assertThat(subject(), attributes(hasKey(CppBinary.OPTIMIZED_ATTRIBUTE)));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native runtime libraries for " + displayName() + "."));
		}
	}
}
