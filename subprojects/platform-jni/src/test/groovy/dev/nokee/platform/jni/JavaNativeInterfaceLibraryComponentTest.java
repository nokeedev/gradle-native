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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.*;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.testers.TargetMachineAwareComponentTester;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.Usage;
import org.gradle.language.cpp.CppBinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.platform.jni.internal.plugins.JniLibraryPlugin.javaNativeInterfaceLibrary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryComponentTest extends AbstractPluginTest implements ComponentTester<JavaNativeInterfaceLibrary>
	, DependencyAwareComponentTester<JavaNativeInterfaceLibraryComponentDependencies>
	, VariantAwareComponentTester<VariantView<NativeLibrary>>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
	, TargetMachineAwareComponentTester
{
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		project.getPluginManager().apply(JvmLanguageBasePlugin.class);
		this.subject = project.getExtensions().getByType(ModelRegistry.class).register(javaNativeInterfaceLibrary("quzu", project)).as(JavaNativeInterfaceLibrary.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.host()));
	}

	@Override
	public JavaNativeInterfaceLibrary subject() {
		return subject;
	}

	@Nested
	class ComponentTasksTest {
		public TaskView<Task> subject() {
			return subject.getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assembleQuzu")));
		}

		@Nested
		class JarTest {
			@Nested
			@PluginRequirement.Require(id = "java")
			class WhenJavaLanguagePluginApplied {
				@Test
				void hasJarTask() {
					assertThat(subject().get(), hasItem(named("jarQuzu")));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "groovy")
			class WhenGroovyLanguagePluginApplied {
				@Test
				void hasJarTask() {
					assertThat(subject().get(), hasItem(named("jarQuzu")));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "org.jetbrains.kotlin.jvm")
			class WhenKotlinLanguagePluginApplied {
				@Test
				void hasJarTask() {
					assertThat(subject().get(), hasItem(named("jarQuzu")));
				}
			}
		}
	}

	@Nested
	class ComponentBinariesTest {
		public BinaryView<Binary> subject() {
			return subject.getBinaries();
		}

		@Nested
		class JvmJarTest {
			@Test
			void noJvmJarBinaryWhenJvmLanguagePluginNotApplied() {
				assertThat(subject().get(), not(hasItem(isA(JvmJarBinary.class))));
			}

			@Nested
			@PluginRequirement.Require(id = "java")
			class WhenJavaLanguagePluginApplied {
				@Test
				void hasJvmJarBinary() {
					assertThat(subject().get(), hasItem(isA(JvmJarBinary.class)));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "groovy")
			class WhenGroovyLanguagePluginApplied {
				@Test
				void hasJvmJarBinary() {
					assertThat(subject().get(), hasItem(isA(JvmJarBinary.class)));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "org.jetbrains.kotlin.jvm")
			class WhenKotlinLanguagePluginApplied {
				@Test
				void hasJvmJarBinary() {
					assertThat(subject().get(), hasItem(isA(JvmJarBinary.class)));
				}
			}
		}
	}

	@Nested
	class ComponentSourcesTest extends JavaNativeInterfaceLibrarySourcesIntegrationTester {
		@Override
		public JavaNativeInterfaceLibrarySources subject() {
			return subject.getSources();
		}
	}

	@Nested
	class ComponentDependenciesTest extends JavaNativeInterfaceNativeComponentDependenciesIntegrationTester {
		@Override
		public JavaNativeInterfaceLibraryComponentDependencies subject() {
			return subject.getDependencies();
		}

		@Override
		public String variantName() {
			return "quzu";
		}

		@Nested
		class ApiTest implements DependencyBucketTester<JavaNativeInterfaceLibraryComponentDependencies> {
			@Override
			public JavaNativeInterfaceLibraryComponentDependencies subject() {
				return ComponentDependenciesTest.this.subject();
			}

			@Override
			public DependencyBucket get(JavaNativeInterfaceLibraryComponentDependencies self) {
				return subject().getApi();
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation) {
				subject().api(notation);
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation, Action<? super ModuleDependency> action) {
				subject().api(notation, action);
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation, @SuppressWarnings("rawtypes") Closure closure) {
				subject().api(notation, closure);
			}

			@Test
			void hasConfigurationWithProperName() {
				assertThat(subject().getApi().getAsConfiguration(), named(variantName() + "Api"));
			}
		}

		@Nested
		class JvmImplementationTest implements DependencyBucketTester<JavaNativeInterfaceLibraryComponentDependencies> {
			@Override
			public JavaNativeInterfaceLibraryComponentDependencies subject() {
				return ComponentDependenciesTest.this.subject();
			}

			@Override
			public DependencyBucket get(JavaNativeInterfaceLibraryComponentDependencies self) {
				return subject().getJvmImplementation();
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation) {
				subject().jvmImplementation(notation);
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation, Action<? super ModuleDependency> action) {
				subject().jvmImplementation(notation, action);
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation, @SuppressWarnings("rawtypes") Closure closure) {
				subject().jvmImplementation(notation, closure);
			}

			@Test
			void hasConfigurationWithProperName() {
				assertThat(subject().getJvmImplementation().getAsConfiguration(), named(variantName() + "JvmImplementation"));
			}
		}

		@Nested
		class JvmRuntimeOnlyTest implements DependencyBucketTester<JavaNativeInterfaceLibraryComponentDependencies> {
			@Override
			public JavaNativeInterfaceLibraryComponentDependencies subject() {
				return ComponentDependenciesTest.this.subject();
			}

			@Override
			public DependencyBucket get(JavaNativeInterfaceLibraryComponentDependencies self) {
				return subject().getJvmRuntimeOnly();
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation) {
				subject().jvmRuntimeOnly(notation);
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation, Action<? super ModuleDependency> action) {
				subject().jvmRuntimeOnly(notation, action);
			}

			@Override
			public void addDependency(JavaNativeInterfaceLibraryComponentDependencies self, Object notation, @SuppressWarnings("rawtypes") Closure closure) {
				subject().jvmRuntimeOnly(notation, closure);
			}

			@Test
			void hasConfigurationWithProperName() {
				assertThat(subject().getJvmRuntimeOnly().getAsConfiguration(), named(variantName() + "JvmRuntimeOnly"));
			}
		}
	}

	public String displayName() {
		return "JNI library ':quzu'";
	}

	@Nested
	class ApiConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("quzuApi");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("API dependencies for " + displayName() + "."));
		}
	}

	@Nested
	class JvmImplementationConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("quzuJvmImplementation");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void extendsFromApiConfiguration() {
			assertThat(subject(), ConfigurationMatchers.extendsFrom(hasItem(named("quzuApi"))));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("JVM implementation dependencies for " + displayName() + "."));
		}
	}

	@Nested
	class JvmRuntimeOnlyConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("quzuJvmRuntimeOnly");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("JVM runtime only dependencies for " + displayName() + "."));
		}
	}

	@Nested
	class NativeImplementationConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("quzuNativeImplementation");
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
			assertThat(project().getConfigurations(), not(hasItem(named("quzuNativeCompileOnly"))));
		}

		abstract class NativeCompileOnlyConfigurationTester {
			public Configuration subject() {
				return project().getConfigurations().getByName("quzuNativeCompileOnly");
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
			return project().getConfigurations().getByName("quzuNativeLinkOnly");
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
			return project().getConfigurations().getByName("quzuNativeRuntimeOnly");
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
	class ApiElementsConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("quzuApiElements");
		}

		@Test
		void isConsumable() {
			assertThat(subject(), ConfigurationMatchers.consumable());
		}

		@Test
		void hasJavaApiUsage() {
			assertThat(subject(), ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("java-api"))));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("API elements for " + displayName() + "."));
		}
	}

	@Nested
	class RuntimeElementsConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("quzuRuntimeElements");
		}

		@Test
		void isConsumable() {
			assertThat(subject(), ConfigurationMatchers.consumable());
		}

		@Test
		void hasJavaRuntimeUsage() {
			assertThat(subject(), ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("java-runtime"))));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Runtime elements for " + displayName() + "."));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return project().getTasks().getByName("assembleQuzu");
		}

		@Test
		void hasBuildGroup() {
			assertThat(subject(), TaskMatchers.group("build"));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of JNI library ':quzu'."));
		}
	}

	@Nested
	class JvmJarBinaryTest {
		@Test
		void noJvmJarBinaryWhenJvmLanguagePluginNotApplied() {
			assertThat(subject.getBinaries().get(), not(hasItem(isA(JvmJarBinary.class))));
		}

		abstract class  BinaryTester extends JvmJarBinaryIntegrationTester {
			@Override
			public String variantName() {
				return "quzu";
			}

			@Override
			public Project project() {
				return project;
			}

			@Override
			public JvmJarBinary subject() {
				return (JvmJarBinary) subject.getBinaries().get().iterator().next();
			}
		}

		@Nested
		@PluginRequirement.Require(id = "java")
		class WhenJavaLanguagePluginApplied extends BinaryTester {}

		@Nested
		@PluginRequirement.Require(id = "groovy")
		class WhenGroovyLanguagePluginApplied extends BinaryTester {}

		@Nested
		@PluginRequirement.Require(id = "org.jetbrains.kotlin.jvm")
		class WhenKotlinLanguagePluginApplied extends BinaryTester {}
	}

	@Nested
	class SingleVariantTest extends JavaNativeInterfaceLibraryVariantIntegrationTester {
		@BeforeEach
		void configureTargetMachines() {
			subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.of("macos-x64")));
			subject();
		}

		@Override
		public JniLibrary subject() {
			return subject.getVariants().get().iterator().next();
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String path() {
			return ":quzu";
		}

		@Override
		public String displayName() {
			return "JNI library ':quzu'";
		}

		@Override
		public String variantName() {
			return "quzu";
		}

		abstract class ResolvableConfigurationAttributeTester {
			public abstract Configuration subject();

			@Test
			void hasOperatingSystemFamilyAttribute() {
				assertThat(subject(), attributes(hasEntry(is(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named("macos"))));
				assertThat(subject(), attributes(hasEntry(is(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named("macos"))));
			}

			@Test
			void hasMachineArchitectureAttribute() {
				assertThat(subject(), attributes(hasEntry(is(MachineArchitecture.ARCHITECTURE_ATTRIBUTE),  named("x64"))));
				assertThat(subject(), attributes(hasEntry(is(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE), named("x86-64"))));
			}

			@Test
			void hasLegacyBuildTypeAttributes() {
				assertThat(subject(), attributes(hasEntry(CppBinary.DEBUGGABLE_ATTRIBUTE, true)));
				assertThat(subject(), attributes(hasEntry(CppBinary.OPTIMIZED_ATTRIBUTE, false)));
			}
		}

		@Nested
		class NativeHeaderSearchPathsAttributesTest {
			@Test
			void noNativeHeaderSearchPathsConfigurationWhenNoNativeLanguageApplied() {
				assertThat(project().getConfigurations(), not(hasItem(named("quzuNativeHeaderSearchPaths"))));
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.c-language")
			class WhenCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuNativeHeaderSearchPaths");
				}

				@Test
				void extendsFromNativeCompileOnlyConfiguration() {
					assertThat(subject(), extendsFrom(hasItem(named("quzuNativeCompileOnly"))));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.cpp-language")
			class WhenCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuNativeHeaderSearchPaths");
				}

				@Test
				void extendsFromNativeCompileOnlyConfiguration() {
					assertThat(subject(), extendsFrom(hasItem(named("quzuNativeCompileOnly"))));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
			class WhenObjectiveCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuNativeHeaderSearchPaths");
				}

				@Test
				void extendsFromNativeCompileOnlyConfiguration() {
					assertThat(subject(), extendsFrom(hasItem(named("quzuNativeCompileOnly"))));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
			class WhenObjectiveCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuNativeHeaderSearchPaths");
				}

				@Test
				void extendsFromNativeCompileOnlyConfiguration() {
					assertThat(subject(), extendsFrom(hasItem(named("quzuNativeCompileOnly"))));
				}
			}
		}

		@Nested
		class NativeLinkLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
			@Override
			public Configuration subject() {
				return project().getConfigurations().getByName("quzuNativeLinkLibraries");
			}
		}

		@Nested
		class NativeRuntimeLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
			@Override
			public Configuration subject() {
				return project().getConfigurations().getByName("quzuNativeRuntimeLibraries");
			}
		}
	}

	@Nested
	class MultipleVariantTest {
		@BeforeEach
		void configureTargetMachine() {
			subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.of("windows-x64"),TargetMachines.of("linux-x86")));
		}

		@Test
		void noJarTaskWhenJvmLanguagePluginNotApplied() {
			// We are checking for the JVM jar task, single variant has its JNI JAR task folded over the JVM JAR task
			assertThat(subject.getTasks().get(), not(hasItem(named("jarQuzu"))));
		}

		@Nested
		class WindowsX64VariantTest extends JavaNativeInterfaceLibraryVariantIntegrationTester {
			@BeforeEach
			void realizeVariant() {
				subject();
			}

			@Override
			public JniLibrary subject() {
				return Iterables.get(subject.getVariants().get(), 0);
			}

			@Override
			public Project project() {
				return project;
			}

			@Override
			public String path() {
				return ":quzu:windowsX64";
			}

			@Override
			public String displayName() {
				return "variant 'windowsX64' of JNI library ':quzu'";
			}

			@Override
			public String variantName() {
				return "quzuWindowsX64";
			}

			@Test
			void nativeImplementationExtendsFromMatchingParentConfiguration() {
				assertThat(project().getConfigurations().getByName("quzuWindowsX64NativeImplementation"),
					extendsFrom(hasItem(named("quzuNativeImplementation"))));
			}

			@Test
			void nativeLinkOnlyExtendsFromMatchingParentConfiguration() {
				assertThat(project().getConfigurations().getByName("quzuWindowsX64NativeLinkOnly"),
					extendsFrom(hasItem(named("quzuNativeLinkOnly"))));
			}

			@Test
			void nativeRuntimeOnlyExtendsFromMatchingParentConfiguration() {
				assertThat(project().getConfigurations().getByName("quzuWindowsX64NativeRuntimeOnly"),
					extendsFrom(hasItem(named("quzuNativeRuntimeOnly"))));
			}

			abstract class ResolvableConfigurationAttributeTester {
				public abstract Configuration subject();

				@Test
				void hasOperatingSystemFamilyAttribute() {
					assertThat(subject(), attributes(hasEntry(is(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named("windows"))));
					assertThat(subject(), attributes(hasEntry(is(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named("windows"))));
				}

				@Test
				void hasMachineArchitectureAttribute() {
					assertThat(subject(), attributes(hasEntry(is(MachineArchitecture.ARCHITECTURE_ATTRIBUTE),  named("x64"))));
					assertThat(subject(), attributes(hasEntry(is(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE), named("x86-64"))));
				}

				@Test
				void hasLegacyBuildTypeAttributes() {
					assertThat(subject(), attributes(hasEntry(CppBinary.DEBUGGABLE_ATTRIBUTE, true)));
					assertThat(subject(), attributes(hasEntry(CppBinary.OPTIMIZED_ATTRIBUTE, false)));
				}
			}

			@Nested
			class NativeHeaderSearchPathsAttributesTest {
				@Nested
				@PluginRequirement.Require(id = "dev.nokee.c-language")
				class WhenCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuWindowsX64NativeHeaderSearchPaths");
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.cpp-language")
				class WhenCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuWindowsX64NativeHeaderSearchPaths");
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
				class WhenObjectiveCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuWindowsX64NativeHeaderSearchPaths");
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
				class WhenObjectiveCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuWindowsX64NativeHeaderSearchPaths");
					}
				}
			}

			@Nested
			class NativeLinkLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuWindowsX64NativeLinkLibraries");
				}
			}

			@Nested
			class NativeRuntimeLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuWindowsX64NativeRuntimeLibraries");
				}
			}
		}

		@Nested
		class LinuxX86VariantTest extends JavaNativeInterfaceLibraryVariantIntegrationTester {
			@BeforeEach
			void realizeVariant() {
				subject();
			}

			@Override
			public JniLibrary subject() {
				return Iterables.get(subject.getVariants().get(), 1);
			}

			@Override
			public Project project() {
				return project;
			}

			@Override
			public String path() {
				return ":quzu:linuxX86";
			}

			@Override
			public String displayName() {
				return "variant 'linuxX86' of JNI library ':quzu'";
			}

			@Override
			public String variantName() {
				return "quzuLinuxX86";
			}

			@Test
			void nativeImplementationExtendsFromMatchingParentConfiguration() {
				assertThat(project().getConfigurations().getByName("quzuLinuxX86NativeImplementation"),
					extendsFrom(hasItem(named("quzuNativeImplementation"))));
			}

			@Test
			void nativeLinkOnlyExtendsFromMatchingParentConfiguration() {
				assertThat(project().getConfigurations().getByName("quzuLinuxX86NativeLinkOnly"),
					extendsFrom(hasItem(named("quzuNativeLinkOnly"))));
			}

			@Test
			void nativeRuntimeOnlyExtendsFromMatchingParentConfiguration() {
				assertThat(project().getConfigurations().getByName("quzuLinuxX86NativeRuntimeOnly"),
					extendsFrom(hasItem(named("quzuNativeRuntimeOnly"))));
			}

			abstract class ResolvableConfigurationAttributeTester {
				public abstract Configuration subject();

				@Test
				void hasOperatingSystemFamilyAttribute() {
					assertThat(subject(), attributes(hasEntry(is(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named("linux"))));
					assertThat(subject(), attributes(hasEntry(is(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named("linux"))));
				}

				@Test
				void hasMachineArchitectureAttribute() {
					assertThat(subject(), attributes(hasEntry(is(MachineArchitecture.ARCHITECTURE_ATTRIBUTE),  named("x86"))));
					assertThat(subject(), attributes(hasEntry(is(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE), named("x86"))));
				}

				@Test
				void hasLegacyBuildTypeAttributes() {
					assertThat(subject(), attributes(hasEntry(CppBinary.DEBUGGABLE_ATTRIBUTE, true)));
					assertThat(subject(), attributes(hasEntry(CppBinary.OPTIMIZED_ATTRIBUTE, false)));
				}
			}

			@Nested
			class NativeHeaderSearchPathsAttributesTest {
				@Nested
				@PluginRequirement.Require(id = "dev.nokee.c-language")
				class WhenCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuLinuxX86NativeHeaderSearchPaths");
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.cpp-language")
				class WhenCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuLinuxX86NativeHeaderSearchPaths");
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
				class WhenObjectiveCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuLinuxX86NativeHeaderSearchPaths");
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
				class WhenObjectiveCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						return project().getConfigurations().getByName("quzuLinuxX86NativeHeaderSearchPaths");
					}
				}
			}

			@Nested
			class NativeLinkLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuLinuxX86NativeLinkLibraries");
				}
			}

			@Nested
			class NativeRuntimeLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					return project().getConfigurations().getByName("quzuLinuxX86NativeRuntimeLibraries");
				}
			}
		}
	}
}
