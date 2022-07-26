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

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.NativeArtifactTypes;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.language.cpp.CppBinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.TaskMatchers.dependsOn;
import static dev.nokee.internal.testing.TaskMatchers.group;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createDependency;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.forUsage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryVariantIntegrationTest extends AbstractPluginTest implements JavaNativeInterfaceLibraryVariantTester {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("reqi", ProjectIdentifier.of(project));
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryVariantRegistrationFactory.class);
		val variantIdentifier = VariantIdentifier.of(DefaultBuildVariant.of(TargetMachines.of("windows-x86")), componentIdentifier);
		subject = registry.register(factory.create(variantIdentifier)).as(JniLibrary.class).get();
	}

	@Override
	public JniLibrary subject() {
		return subject;
	}

	@Test
	void canAccessSharedLibrary() {
		assertThat(subject.getSharedLibrary(), named("reqiWindowsX86SharedLibrary"));
	}

	@Test
	void hasTargetMachineFromVariantIdentifier() {
		assertThat(subject.getTargetMachine(), is(of("windows-x86")));
	}

	@Test
	void usesVariantNameAsBaseNameConvention() {
		subject.getBaseName().set((String) null);
		assertThat(subject.getBaseName(), providerOf("windowsX86"));
	}

	@Test
	void usesJniJarBinaryAsDevelopmentBinaryConvention() {
		subject().getDevelopmentBinary().set((Binary) null);
		assertThat(subject.getDevelopmentBinary(), providerOf(allOf(named("reqiWindowsX86JniJar"), isA(JniJarBinary.class))));
	}

	@Nested
	class ComponentTasksTest {
		public TaskView<Task> subject() {
			return subject.getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assembleReqiWindowsX86")));
		}

		@Test
		void hasJarTask() {
			assertThat(subject().get(), hasItem(allOf(named("jarReqiWindowsX86"), isA(Jar.class))));
		}

		@Test
		void hasSharedLibraryTask() {
			assertThat(subject().get(), hasItem(named("sharedLibraryReqiWindowsX86")));
		}
	}

	@Nested
	class ComponentDependenciesTest extends JavaNativeInterfaceNativeComponentDependenciesIntegrationTester {
		public JavaNativeInterfaceNativeComponentDependencies subject() {
			return subject.getDependencies();
		}

		@Override
		public String variantName() {
			return "reqiWindowsX86";
		}
	}

	@Nested
	class ComponentBinariesTest {
		public BinaryView<Binary> subject() {
			return subject.getBinaries();
		}

		@Test
		void hasJniJarBinary() {
			assertThat(subject().get(), hasItem(allOf(named("reqiWindowsX86JniJar"), isA(JniJarBinary.class))));
		}

		@Test
		void hasSharedLibraryBinary() {
			assertThat(subject().get(), hasItem(allOf(named("reqiWindowsX86SharedLibrary"), isA(SharedLibraryBinary.class))));
		}
	}


	@Nested
	class NativeImplementationConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("reqiWindowsX86NativeImplementation");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native implementation dependencies for variant ':reqi:windowsX86'."));
		}
	}

	@Nested
	class NativeLinkOnlyConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("reqiWindowsX86NativeLinkOnly");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native link only dependencies for variant ':reqi:windowsX86'."));
		}
	}

	@Nested
	class NativeRuntimeOnlyConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName("reqiWindowsX86NativeRuntimeOnly");
		}

		@Test
		void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Native runtime only dependencies for variant ':reqi:windowsX86'."));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return project().getTasks().getByName("assembleReqiWindowsX86");
		}

		@Test
		void hasBuildGroup() {
			assertThat(subject(), group("build"));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of the variant ':reqi:windowsX86'."));
		}
	}

	@Nested
	class SharedLibraryTaskTest {
		public Task subject() {
			return project().getTasks().getByName("sharedLibraryReqiWindowsX86");
		}

		@Test
		void hasBuildGroup() {
			assertThat(subject(), group("build"));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the shared library binary of variant ':reqi:windowsX86'."));
		}

		@Test
		void dependsOnSharedLibraryBinary() {
			assertThat(subject(), dependsOn(hasItem(allOf(named("linkReqiWindowsX86"), isA(LinkSharedLibrary.class)))));
		}
	}

	@Nested
	class ObjectsTaskTest {
		public Task subject() {
			return project().getTasks().getByName("objectsReqiWindowsX86");
		}

		@Test
		void hasBuildGroup() {
			assertThat(subject(), group("build"));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the object files of variant ':reqi:windowsX86'."));
		}
	}

	@Test
	void hasSharedLibraryBinaryLinkedFile() {
		subject().getBaseName().set("heno");
		assertThat(subject().getNativeRuntimeFiles(), hasItem(aFile(withAbsolutePath(endsWith("/build/libs/reqi/windowsX86/heno.dll")))));
	}

	private Configuration runtimeLibraries() {
		return project.getConfigurations().getByName("reqiWindowsX86RuntimeLibraries");
	}

	@Test
	void hasSharedLibraryBinaryRuntimeDependencies() throws IOException {
		val artifact = Files.createTempFile("lib", ".dll").toFile();
		val libraryProducer = ProjectTestUtils.createChildProject(project());
		libraryProducer.getConfigurations().create("runtimeElements",
			configureAsConsumable()
				.andThen(configureAttributes(forUsage(project().getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(configureAttributes(it -> it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
					project().getObjects().named(LibraryElements.class, LibraryElements.DYNAMIC_LIB))))
				.andThen(it -> it.getOutgoing().artifact(artifact, t -> t.setType(NativeArtifactTypes.DYNAMIC_LINK_LIBRARY)))
		);

		runtimeLibraries().getDependencies().add(createDependency(libraryProducer));
		assertThat(subject().getNativeRuntimeFiles(), hasItem(aFile(is(artifact))));
	}

	@Nested
	class JniJarBinaryTest {
		public JniJarBinary subject() {
			return (JniJarBinary) subject.getBinaries().get().stream().filter(it -> it instanceof JniJarBinary).collect(onlyElement());
		}

		@Test
		void doesNotIncludeBinaryNameInJarTaskName() {
			assertThat(subject().getJarTask().map(Task::getName), providerOf("jarReqiWindowsX86"));
		}

		@Test
		void usesVariantBaseNameAndBuildVariantAsJarArchiveBaseName() {
			subject.getBaseName().set("coqu");
			assertThat(subject().getJarTask().flatMap(Jar::getArchiveBaseName), providerOf("coqu-windows-x86"));
		}
	}

//	@Nested
//	class JniJarBinaryTest extends JniJarBinaryIntegrationTester {
//		@Override
//		public String variantName() {
//			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName();
//		}
//
//		@Override
//		public Project project() {
//			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
//		}
//
//		@Override
//		public JniJarBinary subject() {
//			return (JniJarBinary) JavaNativeInterfaceLibraryVariantIntegrationTester.this.subject().getBinaries().get().stream().filter(it -> it instanceof JniJarBinary).collect(MoreCollectors.onlyElement());
//		}
//	}

	@Nested
	class SharedLibraryBinaryTest /*extends SharedLibraryBinaryIntegrationTester*/ {
		public SharedLibraryBinary subject() {
			return subject.getSharedLibrary();
		}

		@Test
		void usesVariantNameAsBaseNameConvention() {
			subject.getBaseName().set("huca");
			subject().getBaseName().set((String) null);
			assertThat(subject().getBaseName(), providerOf("huca"));
		}
//
//		@Override
//		public Project project() {
//			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.project();
//		}
//
//		@Override
//		public String variantName() {
//			return JavaNativeInterfaceLibraryVariantIntegrationTester.this.variantName();
//		}
//
//		@Override
//		public String displayName() {
//			return "shared library binary '" + path() + ":sharedLibrary'";
//		}


		@Nested
		class NativeLinkLibrariesConfigurationTest {
			public Configuration subject() {
				return realize(project().getConfigurations().getByName("reqiWindowsX86LinkLibraries"));
			}

			@Test
			void extendsFromNativeImplementationConfiguration() {
				assertThat(subject(), extendsFrom(hasItem(named("reqiWindowsX86NativeImplementation"))));
			}

			@Test
			void extendsFromNativeLinkOnlyConfiguration() {
				assertThat(subject(), extendsFrom(hasItem(named("reqiWindowsX86NativeLinkOnly"))));
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
		}

		@Nested
		class NativeRuntimeLibrariesConfigurationTest {
			public Configuration subject() {
				return realize(project().getConfigurations().getByName("reqiWindowsX86RuntimeLibraries"));
			}

			@Test
			void extendsFromNativeImplementationConfiguration() {
				assertThat(subject(), extendsFrom(hasItem(named("reqiWindowsX86NativeImplementation"))));
			}

			@Test
			void extendsFromNativeRuntimeOnlyConfiguration() {
				assertThat(subject(), extendsFrom(hasItem(named("reqiWindowsX86NativeRuntimeOnly"))));
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
		}
	}

	private static Configuration realize(Configuration configuration) {
		((ConfigurationInternal) configuration).preventFromFurtherMutation();
		return configuration;
	}
}
