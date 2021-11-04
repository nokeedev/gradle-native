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

import com.google.common.collect.MoreCollectors;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.CSourceSetIntegrationTester;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.CppSourceSetIntegrationTester;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSetIntegrationTester;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSetIntegrationTester;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.testers.BinaryAwareComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.TaskAwareComponentTester;
import dev.nokee.platform.base.testers.VariantTester;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.language.cpp.CppBinary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.TaskMatchers.group;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class JavaNativeInterfaceLibraryVariantIntegrationTester implements VariantTester<JniLibrary>
	, DependencyAwareComponentTester<JavaNativeInterfaceNativeComponentDependencies>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
{
	public abstract JniLibrary subject();

	public abstract Project project();

	public abstract String displayName();

	public abstract String variantName();

	public abstract String path();

	@Nested
	class ComponentSourcesTest {
		public SourceView<LanguageSourceSet> subject() {
			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getSources();
		}

		@Nested
		class CTest {
			@Test
			void noCSourceSetWhenCLanguagePluginNotApplied() {
				assertThat(subject().get(), not(hasItem(isA(CSourceSet.class))));
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.c-language")
			class WhenCLanguagePluginApplied {
				@Test
				void hasCSourceSet() {
					assertThat(subject().get(), hasItem(isA(CSourceSet.class)));
				}
			}
		}

		@Nested
		class CppTest {
			@Test
			void noCppSourceSetWhenObjectiveCLanguagePluginNotApplied() {
				assertThat(subject().get(), not(hasItem(isA(CppSourceSet.class))));
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.cpp-language")
			class WhenCppLanguagePluginApplied {
				@Test
				void hasCppSourceSet() {
					assertThat(subject().get(), hasItem(isA(CppSourceSet.class)));
				}
			}
		}

		@Nested
		class ObjectiveCTest {
			@Test
			void noObjectiveCSourceSetWhenObjectiveCLanguagePluginNotApplied() {
				assertThat(subject().get(), not(hasItem(isA(ObjectiveCSourceSet.class))));
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
			class WhenObjectiveCLanguagePluginApplied {
				@Test
				void hasObjectiveCSourceSet() {
					assertThat(subject().get(), hasItem(isA(ObjectiveCSourceSet.class)));
				}
			}
		}

		@Nested
		class ObjectiveCppTest {
			@Test
			void noObjectiveCppSourceSetWhenObjectiveCppLanguagePluginNotApplied() {
				assertThat(subject().get(), not(hasItem(isA(ObjectiveCppSourceSet.class))));
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
			class WhenObjectiveCppLanguagePluginApplied {
				@Test
				void hasObjectiveCppSourceSet() {
					assertThat(subject().get(), hasItem(isA(ObjectiveCppSourceSet.class)));
				}
			}
		}
	}

	@Nested
	class ComponentTasksTest {
		public TaskView<Task> subject() {
			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assemble" + capitalize(variantName()))));
		}

		@Test
		void hasJarTask() {
			assertThat(subject().get(), hasItem(named("jar" + capitalize(variantName()))));
		}
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

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return project().getTasks().getByName("assemble" + capitalize(variantName()));
		}

		@Test
		void hasBuildGroup() {
			assertThat(subject(), group("build"));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of " + displayName() + "."));
		}
	}

	@Nested
	class JniJarBinaryTest extends JniJarBinaryIntegrationTester {
		@Override
		public String variantName() {
			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName();
		}

		@Override
		public Project project() {
			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
		}

		@Override
		public JniJarBinary subject() {
			return (JniJarBinary) JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getBinaries().get().stream().filter(it -> it instanceof JniJarBinary).collect(MoreCollectors.onlyElement());
		}
	}

	@Nested
	class CSourceSetTest {
		@Test
		void noCSourceSetWhenCLanguagePluginNotApplied() {
			assertThat(subject().getSources().get(), not(hasItem(isA(CSourceSet.class))));
		}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.c-language")
		class WhenCLanguagePluginApplied extends CSourceSetIntegrationTester {
			@Override
			public CSourceSet subject() {
				return (CSourceSet) JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getSources().get().stream().filter(it -> it instanceof CSourceSet).collect(MoreCollectors.onlyElement());
			}

			@Override
			public Project project() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
			}

			@Override
			public String name() {
				return "c";
			}

			@Override
			public String variantName() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName() + "C";
			}

			@Override
			public String displayName() {
				return "C sources '" + path() + ":c'";
			}
		}
	}

	@Nested
	class CppTest {
		@Test
		void noCppSourceSetWhenObjectiveCLanguagePluginNotApplied() {
			assertThat(subject().getSources().get(), not(hasItem(isA(CppSourceSet.class))));
		}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.cpp-language")
		class WhenCppLanguagePluginApplied extends CppSourceSetIntegrationTester {
			@Override
			public CppSourceSet subject() {
				return (CppSourceSet) JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getSources().get().stream().filter(it -> it instanceof CppSourceSet).collect(MoreCollectors.onlyElement());
			}

			@Override
			public Project project() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
			}

			@Override
			public String name() {
				return "cpp";
			}

			@Override
			public String variantName() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName() + "Cpp";
			}

			@Override
			public String displayName() {
				return "C++ sources '" + path() + ":cpp'";
			}
		}
	}

	@Nested
	class ObjectiveCTest {
		@Test
		void noObjectiveCSourceSetWhenObjectiveCLanguagePluginNotApplied() {
			assertThat(subject().getSources().get(), not(hasItem(isA(ObjectiveCSourceSet.class))));
		}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
		class WhenObjectiveCLanguagePluginApplied extends ObjectiveCSourceSetIntegrationTester {
			@Override
			public ObjectiveCSourceSet subject() {
				return (ObjectiveCSourceSet) JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getSources().get().stream().filter(it -> it instanceof ObjectiveCSourceSet).collect(MoreCollectors.onlyElement());
			}

			@Override
			public Project project() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
			}

			@Override
			public String name() {
				return "objectiveC";
			}

			@Override
			public String variantName() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName() + "ObjectiveC";
			}

			@Override
			public String displayName() {
				return "Objective-C sources '" + path() + ":objectiveC'";
			}
		}
	}

	@Nested
	class ObjectiveCppTest {
		@Test
		void noObjectiveCppSourceSetWhenObjectiveCppLanguagePluginNotApplied() {
			assertThat(subject().getSources().get(), not(hasItem(isA(ObjectiveCppSourceSet.class))));
		}

		@Nested
		@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
		class WhenObjectiveCppLanguagePluginApplied extends ObjectiveCppSourceSetIntegrationTester {
			@Override
			public ObjectiveCppSourceSet subject() {
				return (ObjectiveCppSourceSet) JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getSources().get().stream().filter(it -> it instanceof ObjectiveCppSourceSet).collect(MoreCollectors.onlyElement());
			}

			@Override
			public Project project() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
			}

			@Override
			public String name() {
				return "objectiveCpp";
			}

			@Override
			public String variantName() {
				return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName() + "ObjectiveCpp";
			}

			@Override
			public String displayName() {
				return "Objective-C++ sources '" + path() + ":objectiveCpp'";
			}
		}
	}
}
