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
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.testers.BinaryAwareComponentTester;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.DependencyBucketTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.HasDevelopmentVariantTester;
import dev.nokee.platform.base.testers.TaskAwareComponentTester;
import dev.nokee.platform.base.testers.VariantAwareComponentTester;
import dev.nokee.platform.base.testers.VariantDimensionsIntegrationTester;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.platform.nativebase.testers.TargetMachineAwareComponentTester;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.consumable;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.ConfigurationMatchers.hasPublishArtifact;
import static dev.nokee.internal.testing.ConfigurationMatchers.ofFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileBaseNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.TaskMatchers.dependsOn;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.host;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(type = CLanguageBasePlugin.class)
@PluginRequirement.Require(type = JvmLanguageBasePlugin.class)
class JavaNativeInterfaceLibraryComponentIntegrationTest extends AbstractPluginTest implements ComponentTester<JavaNativeInterfaceLibrary>
	, DependencyAwareComponentTester<JavaNativeInterfaceLibraryComponentDependencies>
	, VariantAwareComponentTester<VariantView<NativeLibrary>>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
	, TargetMachineAwareComponentTester
	, HasBaseNameTester
	, HasDevelopmentVariantTester
{
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val identifier = ComponentIdentifier.builder().name(ComponentName.of("quzu")).withProjectIdentifier(ProjectIdentifier.of(project)).displayName("JNI library component").build();
		this.subject = project.getExtensions().getByType(ModelRegistry.class).register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(host()));
	}

	@Override
	public JavaNativeInterfaceLibrary subject() {
		return subject;
	}

	@Test
	void usesComponentNameAsBaseNameConvention() {
		subject.getBaseName().set((String) null);
		assertThat(subject.getBaseName(), providerOf("quzu"));
	}

	@Test
	void ignoresUnbuildableVariantDuringDevelopmentVariantSelection() {
		subject.getTargetMachines().set(ImmutableSet.of(of("unbuildable-unbuildable"), host()));
		((ProjectInternal) project).getModelRegistry().find("toolChains", NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolChain -> {
			toolChain.target("unbuildableunbuildable", t -> {
				// Make the target unbuildable
				t.getLinker().setExecutable("not-found");
				t.getAssembler().setExecutable("not-found");
				t.getcCompiler().setExecutable("not-found");
				t.getCppCompiler().setExecutable("not-found");
				t.getObjcCompiler().setExecutable("not-found");
				t.getObjcppCompiler().setExecutable("not-found");
				t.getStaticLibArchiver().setExecutable("not-found");
			});
		});
		assertThat("uses host variant", subject.getDevelopmentVariant(), providerOf(named("quzu" + capitalize(host().getOperatingSystemFamily().getName()) + capitalize(host().getArchitecture().getName()))));
	}

	@Test
	@Disabled // Cross-build not exactly finished
	void canUseCrossBuildVariantDuringDevelopmentVariantSelectionIfNoOtherBuildableVariantExists() {
		subject.getTargetMachines().set(ImmutableSet.of(of("munix-aarch64"), of("unbuildable-unbuildable")));
		((ProjectInternal) project).getModelRegistry().find("toolChains", NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolChain -> {
			toolChain.target("unbuildableunbuildable", t -> {
				// Make the target unbuildable
				t.getLinker().setExecutable("not-found");
				t.getAssembler().setExecutable("not-found");
				t.getcCompiler().setExecutable("not-found");
				t.getCppCompiler().setExecutable("not-found");
				t.getObjcCompiler().setExecutable("not-found");
				t.getObjcppCompiler().setExecutable("not-found");
				t.getStaticLibArchiver().setExecutable("not-found");
			});
		});
		((ProjectInternal) project).getModelRegistry().find("toolChains", NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolChain -> toolChain.target("munixaarch64"));
		assertThat("uses cross-build variant", subject.getDevelopmentVariant(), providerOf(named("quzuMunixAarch64")));
	}

	@Nested
	class VariantDimensionsTest extends VariantDimensionsIntegrationTester {
		@Override
		public VariantAwareComponent<?> subject() {
			return subject;
		}
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
		return "JNI library component ':quzu'";
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
			return realize(project().getConfigurations().getByName("quzuJvmImplementation"));
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
			return realize(project().getConfigurations().getByName("quzuApiElements"));
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
			return realize(project().getConfigurations().getByName("quzuRuntimeElements"));
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
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of " + displayName() + "."));
		}

		@Test
		void usesDevelopmentVariantAsTaskDependency() {
			subject.getTargetMachines().set(ImmutableSet.of(of("unbuildable-unbuildable"), host()));
			((ProjectInternal) project).getModelRegistry().find("toolChains", NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolChain -> {
				toolChain.target("unbuildableunbuildable", t -> {
					// Make the target unbuildable
					t.getLinker().setExecutable("not-found");
					t.getAssembler().setExecutable("not-found");
					t.getcCompiler().setExecutable("not-found");
					t.getCppCompiler().setExecutable("not-found");
					t.getObjcCompiler().setExecutable("not-found");
					t.getObjcppCompiler().setExecutable("not-found");
					t.getStaticLibArchiver().setExecutable("not-found");
				});
			});
			assertThat("has buildable JAR", subject(), dependsOn(hasItem(
				allOf(named("quzu" + capitalize(host().getOperatingSystemFamily().getName()) + capitalize(host().getArchitecture().getName()) + "JniJar")))));
			assertThat("has no unbuildable JAR", subject(), dependsOn(not(hasItem(named("jarQuzuUnbuildableUnbuildable")))));
		}
	}

	@Nested
	class ApiElementsTest {
		public Configuration subject() {
			return realize(project.getConfigurations().getByName("quzuApiElements"));
		}

		@Test
		void isConsumable() {
			assertThat(subject(), consumable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("API elements for " + displayName() + "."));
		}

		@Test
		void extendsFromApiConfiguration() {
			assertThat(subject(), extendsFrom(hasItem(named("quzuApi"))));
		}

		@Test
		void hasJavaApiUsageAttribute() {
			assertThat(subject(), attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("java-api"))));
		}

		@Test
		void hasJarLibraryElementsAttribute() {
			assertThat(subject(), attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named("jar"))));
		}
	}

	@Nested
	class RuntimeElementsTest {
		public Configuration subject() {
			return realize(project.getConfigurations().getByName("quzuRuntimeElements"));
		}

		@Test
		void isConsumable() {
			assertThat(subject(), consumable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Runtime elements for " + displayName() + "."));
		}

		@Test
		void extendsFromApiConfiguration() {
			assertThat(subject(), extendsFrom(hasItem(named("quzuApi"))));
		}

		@Test
		void hasJavaRuntimeUsageAttribute() {
			assertThat(subject(), attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("java-runtime"))));
		}

		@Test
		void hasJarLibraryElementsAttribute() {
			assertThat(subject(), attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named("jar"))));
		}

		@Test
		void hasOnlyBuildableJarAsOutgoingArtifacts() {
			subject.getTargetMachines().set(ImmutableSet.of(of("unbuildable-unbuildable"), host()));
			((ProjectInternal) project).getModelRegistry().find("toolChains", NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolChain -> toolChain.target("unbuildableunbuildable", t -> {
				// Make the target unbuildable
				t.getLinker().setExecutable("not-found");
				t.getcCompiler().setExecutable("not-found");
				t.getCppCompiler().setExecutable("not-found");
			}));
			assertThat("has only host JAR, not unbuildable JAR", subject(), hasPublishArtifact(ofFile(aFileBaseNamed(
				allOf(startsWith("quzu-"), not("quzu-unbuildable-unbuildable"))))));
		}

		@Test
		void hasCrossCompileBuildableJarAsOutgoingArtifacts() {
			subject.getTargetMachines().set(ImmutableSet.of(of("munix-aarch64"), host()));
			((ProjectInternal) project).getModelRegistry().find("toolChains", NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolChain -> toolChain.target("munixaarch64"));
			assertThat(subject(), hasPublishArtifact(ofFile(aFileBaseNamed("quzu-munix-aarch64"))));
		}
	}

	@Nested
	class ResourcePathConventionTest {
		@Test
		void hasEmptyResourcePathWhenGroupIsEmptyAndSingleVariant() {
			project.setGroup("");
			assertThat(allResourcePaths(), contains(providerOf(emptyString())));
		}

		@Test
		void usesVariantAmbiguousDimensionAsResourcePathForAllVariant() {
			subject.getTargetMachines().set(ImmutableSet.of(of("windows-x64"), of("macos-x64")));
			assertThat(allResourcePaths(), contains(providerOf("windows"), providerOf("macos")));
		}

		@Test
		void includesAllVariantAmbiguousDimensionsAsKebabCaseInResourcePathForAllVariant() {
			subject.getTargetMachines().set(ImmutableSet.of(of("windows-x86"), of("linux-x64")));
			assertThat(allResourcePaths(), contains(providerOf("windows-x86"), providerOf("linux-x64")));
		}

		private List<Provider<String>> allResourcePaths() {
			return subject.getVariants().get().stream().map(it -> it.getResourcePath().value((String) null)).collect(toList());
		}
	}

//	@Nested
//	class JvmJarBinaryTest {
//		@Test
//		void noJvmJarBinaryWhenJvmLanguagePluginNotApplied() {
//			assertThat(subject.getBinaries().get(), not(hasItem(isA(JvmJarBinary.class))));
//		}
//
//		abstract class  BinaryTester extends JvmJarBinaryIntegrationTester {
//			@Override
//			public String variantName() {
//				return "quzu";
//			}
//
//			@Override
//			public Project project() {
//				return project;
//			}
//
//			@Override
//			public JvmJarBinary subject() {
//				return (JvmJarBinary) subject.getBinaries().get().iterator().next();
//			}
//		}
//
//		@Nested
//		@PluginRequirement.Require(id = "java")
//		class WhenJavaLanguagePluginApplied extends BinaryTester {}
//
//		@Nested
//		@PluginRequirement.Require(id = "groovy")
//		class WhenGroovyLanguagePluginApplied extends BinaryTester {}
//
//		@Nested
//		@PluginRequirement.Require(id = "org.jetbrains.kotlin.jvm")
//		class WhenKotlinLanguagePluginApplied extends BinaryTester {}
//	}

	@Nested
	class SingleVariantTest {
		@BeforeEach
		void configureTargetMachines() {
			subject.getTargetMachines().set(ImmutableSet.of(of("macos-x64")));
		}

		public JniLibrary subject() {
			return subject.getVariants().get().iterator().next();
		}

		@Test
		void usesComponentBaseNameAsBaseNameConvention() {
			subject.getBaseName().set("jaru");
			subject().getBaseName().set((String) null);
			assertThat(subject().getBaseName(), providerOf("jaru"));
		}

		@Test
		void putsJniJarDirectlyInLibsDirectory() {
			assertThat(subject().getJavaNativeInterfaceJar().getJarTask().flatMap(Jar::getDestinationDirectory),
				providerOf(aFile(withAbsolutePath(endsWith("/build/libs")))));
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
				SingleVariantTest.this.subject(); // realize variant
				assertThat(project().getConfigurations(), not(hasItem(named(endsWith("HeaderSearchPaths")))));
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.c-language")
			class WhenCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					SingleVariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuCHeaderSearchPaths"));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.cpp-language")
			class WhenCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					SingleVariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuCppHeaderSearchPaths"));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
			class WhenObjectiveCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					SingleVariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuObjectiveCHeaderSearchPaths"));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
			class WhenObjectiveCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					SingleVariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuObjectiveCppHeaderSearchPaths"));
				}
			}
		}

		@Nested
		class NativeLinkLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
			@Override
			public Configuration subject() {
				SingleVariantTest.this.subject(); // realize variant
				return realize(project().getConfigurations().getByName("quzuLinkLibraries"));
			}
		}

		@Nested
		class NativeRuntimeLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
			@Override
			public Configuration subject() {
				SingleVariantTest.this.subject(); // realize variant
				return realize(project().getConfigurations().getByName("quzuRuntimeLibraries"));
			}
		}

		@Nested
		class NativeCompileTaskTest {
			@Nested
			@PluginRequirement.Require(id = "dev.nokee.c-language")
			class WhenCLanguagePluginApplied {
				public CCompileTask subject() {
					SingleVariantTest.this.subject(); // realize variant
					return (CCompileTask) project().getTasks().getByName("compileQuzuC");
				}

				@Test
				void hasTargetPlatform() {
					assertThat(subject().getTargetPlatform(), providerOf(named("macosx86-64")));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.cpp-language")
			class WhenCppLanguagePluginApplied {
				public CppCompileTask subject() {
					SingleVariantTest.this.subject(); // realize variant
					return (CppCompileTask) project().getTasks().getByName("compileQuzuCpp");
				}

				@Test
				void hasTargetPlatform() {
					assertThat(subject().getTargetPlatform(), providerOf(named("macosx86-64")));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
			class WhenObjectiveCLanguagePluginApplied {
				public ObjectiveCCompileTask subject() {
					SingleVariantTest.this.subject(); // realize variant
					return (ObjectiveCCompileTask) project().getTasks().getByName("compileQuzuObjectiveC");
				}

				@Test
				void hasTargetPlatform() {
					assertThat(subject().getTargetPlatform(), providerOf(named("macosx86-64")));
				}
			}

			@Nested
			@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
			class WhenObjectiveCppLanguagePluginApplied {
				public ObjectiveCppCompileTask subject() {
					SingleVariantTest.this.subject(); // realize variant
					return (ObjectiveCppCompileTask) project().getTasks().getByName("compileQuzuObjectiveCpp");
				}

				@Test
				void hasTargetPlatform() {
					assertThat(subject().getTargetPlatform(), providerOf(named("macosx86-64")));
				}
			}
		}

		@Nested
		class NativeLinkTask {
			public LinkSharedLibraryTask subject() {
				SingleVariantTest.this.subject(); // realize variant
				return (LinkSharedLibraryTask) project().getTasks().getByName("linkQuzu");
			}

			@Test
			void hasTargetPlatform() {
				assertThat(subject().getTargetPlatform(), providerOf(named("macosx86-64")));
			}
		}

		@Nested
		class ComponentComponentBinaries {
			public BinaryView<Binary> subject() {
				return subject.getBinaries();
			}

			@Test
			void hasJniJar() {
				assertThat(subject().get(), hasItem(isA(JniJarBinary.class)));
			}
		}
	}

	@Nested
	class MultipleVariantTest {
		@BeforeEach
		void configureTargetMachine() {
			subject.getTargetMachines().set(ImmutableSet.of(of("windows-x64"), of("linux-x86")));
		}

		@Test
		void noJarTaskWhenJvmLanguagePluginNotApplied() {
			// We are checking for the JVM jar task, single variant has its JNI JAR task folded over the JVM JAR task
			assertThat(subject.getTasks().get(), not(hasItem(named("jarQuzu"))));
		}

		@Nested
		class WindowsX64VariantTest {
			public JniLibrary subject() {
				return Iterables.get(subject.getVariants().get(), 0);
			}

			@Test
			void usesComponentBaseNameAsBaseNameConvention() {
				subject.getBaseName().set("zihu");
				subject().getBaseName().set((String) null);
				assertThat(subject().getBaseName(), providerOf("zihu"));
			}

			@Test
			void putsJniJarInComponentNamedFolderInsideLibsDirectory() {
				assertThat(subject().getJavaNativeInterfaceJar().getJarTask().flatMap(Jar::getDestinationDirectory),
					providerOf(aFile(withAbsolutePath(endsWith("/build/libs/quzu")))));
			}

			@Test
			void nativeImplementationExtendsFromMatchingParentConfiguration() {
				subject(); // realize variant
				assertThat(project().getConfigurations().getByName("quzuWindowsX64NativeImplementation"),
					extendsFrom(hasItem(named("quzuNativeImplementation"))));
			}

			@Test
			void nativeLinkOnlyExtendsFromMatchingParentConfiguration() {
				subject(); // realize variant
				assertThat(project().getConfigurations().getByName("quzuWindowsX64NativeLinkOnly"),
					extendsFrom(hasItem(named("quzuNativeLinkOnly"))));
			}

			@Test
			void nativeRuntimeOnlyExtendsFromMatchingParentConfiguration() {
				subject(); // realize variant
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
						WindowsX64VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuWindowsX64CHeaderSearchPaths"));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.cpp-language")
				class WhenCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuWindowsX64CppHeaderSearchPaths"));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
				class WhenObjectiveCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuWindowsX64ObjectiveCHeaderSearchPaths"));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
				class WhenObjectiveCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuWindowsX64ObjectiveCppHeaderSearchPaths"));
					}
				}
			}

			@Nested
			class NativeLinkLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					WindowsX64VariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuWindowsX64LinkLibraries"));
				}
			}

			@Nested
			class NativeRuntimeLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					WindowsX64VariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuWindowsX64RuntimeLibraries"));
				}
			}

			@Nested
			class NativeCompileTaskTest {
				@Nested
				@PluginRequirement.Require(id = "dev.nokee.c-language")
				class WhenCLanguagePluginApplied {
					public CCompileTask subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return (CCompileTask) project().getTasks().getByName("compileQuzuWindowsX64C");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86-64")));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.cpp-language")
				class WhenCppLanguagePluginApplied {
					public CppCompileTask subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return (CppCompileTask) project().getTasks().getByName("compileQuzuWindowsX64Cpp");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86-64")));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
				class WhenObjectiveCLanguagePluginApplied {
					public ObjectiveCCompileTask subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return (ObjectiveCCompileTask) project().getTasks().getByName("compileQuzuWindowsX64ObjectiveC");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86-64")));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
				class WhenObjectiveCppLanguagePluginApplied {
					public ObjectiveCppCompileTask subject() {
						WindowsX64VariantTest.this.subject(); // realize variant
						return (ObjectiveCppCompileTask) project().getTasks().getByName("compileQuzuWindowsX64ObjectiveCpp");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86-64")));
					}
				}
			}

			@Nested
			class NativeLinkTask {
				public LinkSharedLibraryTask subject() {
					WindowsX64VariantTest.this.subject(); // realize variant
					return (LinkSharedLibraryTask) project().getTasks().getByName("linkQuzuWindowsX64");
				}

				@Test
				void hasTargetPlatform() {
					assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86-64")));
				}
			}
		}

		@Nested
		class LinuxX86VariantTest {
			public JniLibrary subject() {
				return Iterables.get(subject.getVariants().get(), 1);
			}

			@Test
			void usesComponentBaseNameAsBaseNameConvention() {
				subject.getBaseName().set("cebo");
				subject().getBaseName().set((String) null);
				assertThat(subject().getBaseName(), providerOf("cebo"));
			}

			@Test
			void putsJniJarInComponentNamedFolderInsideLibsDirectory() {
				subject(); // realize variant
				assertThat(subject().getJavaNativeInterfaceJar().getJarTask().flatMap(Jar::getDestinationDirectory),
					providerOf(aFile(withAbsolutePath(endsWith("/build/libs/quzu")))));
			}

			@Test
			void nativeImplementationExtendsFromMatchingParentConfiguration() {
				subject(); // realize variant
				assertThat(project().getConfigurations().getByName("quzuLinuxX86NativeImplementation"),
					extendsFrom(hasItem(named("quzuNativeImplementation"))));
			}

			@Test
			void nativeLinkOnlyExtendsFromMatchingParentConfiguration() {
				subject(); // realize variant
				assertThat(project().getConfigurations().getByName("quzuLinuxX86NativeLinkOnly"),
					extendsFrom(hasItem(named("quzuNativeLinkOnly"))));
			}

			@Test
			void nativeRuntimeOnlyExtendsFromMatchingParentConfiguration() {
				subject(); // realize variant
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
						LinuxX86VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuLinuxX86CHeaderSearchPaths"));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.cpp-language")
				class WhenCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuLinuxX86CppHeaderSearchPaths"));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
				class WhenObjectiveCLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuLinuxX86ObjectiveCHeaderSearchPaths"));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
				class WhenObjectiveCppLanguagePluginApplied extends ResolvableConfigurationAttributeTester {
					@Override
					public Configuration subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return realize(project().getConfigurations().getByName("quzuLinuxX86ObjectiveCppHeaderSearchPaths"));
					}
				}
			}

			@Nested
			class NativeLinkLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					LinuxX86VariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuLinuxX86LinkLibraries"));
				}
			}

			@Nested
			class NativeRuntimeLibrariesAttributesTest extends ResolvableConfigurationAttributeTester {
				@Override
				public Configuration subject() {
					LinuxX86VariantTest.this.subject(); // realize variant
					return realize(project().getConfigurations().getByName("quzuLinuxX86RuntimeLibraries"));
				}
			}

			@Nested
			class NativeCompileTaskTest {
				@Nested
				@PluginRequirement.Require(id = "dev.nokee.c-language")
				class WhenCLanguagePluginApplied {
					public CCompileTask subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return (CCompileTask) project().getTasks().getByName("compileQuzuLinuxX86C");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86")));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.cpp-language")
				class WhenCppLanguagePluginApplied {
					public CppCompileTask subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return (CppCompileTask) project().getTasks().getByName("compileQuzuLinuxX86Cpp");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86")));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
				class WhenObjectiveCLanguagePluginApplied {
					public ObjectiveCCompileTask subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return (ObjectiveCCompileTask) project().getTasks().getByName("compileQuzuLinuxX86ObjectiveC");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86")));
					}
				}

				@Nested
				@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
				class WhenObjectiveCppLanguagePluginApplied {
					public ObjectiveCppCompileTask subject() {
						LinuxX86VariantTest.this.subject(); // realize variant
						return (ObjectiveCppCompileTask) project().getTasks().getByName("compileQuzuLinuxX86ObjectiveCpp");
					}

					@Test
					void hasTargetPlatform() {
						assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86")));
					}
				}
			}

			@Nested
			class NativeLinkTask {
				public LinkSharedLibraryTask subject() {
					LinuxX86VariantTest.this.subject(); // realize variant
					return (LinkSharedLibraryTask) project().getTasks().getByName("linkQuzuLinuxX86");
				}

				@Test
				void hasTargetPlatform() {
					assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86")));
				}
			}
		}
	}

	public static Configuration realize(Configuration configuration) {
		((ConfigurationInternal) configuration).preventFromFurtherMutation();
		return configuration;
	}
}
