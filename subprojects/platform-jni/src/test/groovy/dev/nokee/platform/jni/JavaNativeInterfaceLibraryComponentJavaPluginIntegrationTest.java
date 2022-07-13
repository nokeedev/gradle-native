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
import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.testers.SourceTester;
import dev.nokee.language.c.internal.plugins.CSourceSetSpec;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec;
import dev.nokee.language.jvm.HasJavaSourceSet;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetSpec;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetSpec;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.dependencies.DependencyBuckets;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.ConfigurationMatchers.ofFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "java")
class JavaNativeInterfaceLibraryComponentJavaPluginIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		val identifier = ComponentIdentifier.of("qezu", ProjectIdentifier.ofRootProject());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		this.subject = registry.register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.host()));
	}

	@Test
	void hasJavaSourceSetWhenJavaPluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("qezuJava"), isA(JavaSourceSet.class))));
	}

	@Test
	void hasJvmJarBinaryWhenJavaPluginApplied() {
		assertThat(subject.getBinaries().get(), hasItem(allOf(named("qezuJvmJar"), isA(JvmJarBinary.class))));
	}

	@Test
	void runtimeOnlyConfigurationExtendsFromJvmRuntimeOnlyConfiguration() {
		assertThat(DependencyBuckets.finalize(project.getConfigurations().getByName("qezuRuntimeOnly")), extendsFrom(hasItem(named("qezuJvmRuntimeOnly"))));
	}

	@Test
	void implementationConfigurationExtendsFromJvmImplementationConfiguration() {
		assertThat(DependencyBuckets.finalize(project.getConfigurations().getByName("qezuImplementation")), extendsFrom(hasItem(named("qezuJvmImplementation"))));
	}

	@Nested
	class JvmJarBinaryTest {
		@BeforeEach
		void configureMultipleVariant() {
			// To avoid JVM and JNI Jar task folding, we configure multiple variant
			subject.getTargetMachines().set(ImmutableSet.of(of("windows-x86"), of("macos-x64")));
		}

		public JvmJarBinary subject() {
			return (JvmJarBinary) subject.getBinaries().get().stream().filter(it -> it instanceof JvmJarBinary).collect(onlyElement());
		}

		@Test
		void doesNotIncludeBinaryNameInJarTaskName() {
			assertThat(subject().getJarTask().map(Task::getName), providerOf("jarQezu"));
		}

		@Test
		void usesComponentBaseNameAsJarArchiveBaseName() {
			subject.getBaseName().set("hexu");
			assertThat(subject().getJarTask().flatMap(Jar::getArchiveBaseName), providerOf("hexu"));
		}
	}

	@Nested
	class JavaComponentSourcesTest implements SourceTester<HasJavaSourceSet, JavaSourceSet> {
		@Override
		public HasJavaSourceSet subject() {
			return subject.getSources();
		}

		@Override
		public NamedDomainObjectProvider<? extends LanguageSourceSet> get(HasJavaSourceSet self) {
			return self.getJava();
		}

		@Override
		public void configure(HasJavaSourceSet self, Action<? super JavaSourceSet> action) {
			self.java(action);
		}

		@Override
		public void configure(HasJavaSourceSet self, @SuppressWarnings("rawtypes") Closure closure) {
			self.java(closure);
		}
	}

	@Nested
	class CompileTaskTest {
		public JavaCompile subject() {
			return (JavaCompile) project.getTasks().getByName("compileQezuJava");
		}

		@Test
		void hasHeaderOutputDirectoryUnderGeneratedJniHeadersInsideBuildDirectory() {
			assertThat(subject().getOptions().getHeaderOutputDirectory(),
				providerOf(aFile(withAbsolutePath(containsString("/build/generated/jni-headers/")))));
		}

		@Test
		void usesComponentNameInHeaderOutputDirectory() {
			assertThat(subject().getOptions().getHeaderOutputDirectory(),
				providerOf(aFileNamed("qezu")));
		}
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.c-language")
	void attachesJniHeaderDirectoryToCHeaders() {
		subject.getSources().get(); // force realize until named can realize
		assertThat(subject.getSources().named("c", CSourceSetSpec.class).get().getHeaders().getSourceDirectories(),
			hasItem(aFile(withAbsolutePath(endsWith("/generated/jni-headers/qezu")))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.cpp-language")
	void attachesJniHeaderDirectoryToCppHeaders() {
		subject.getSources().get(); // force realize until named can realize
		assertThat(subject.getSources().named("cpp", CppSourceSetSpec.class).get().getHeaders().getSourceDirectories(),
			hasItem(aFile(withAbsolutePath(endsWith("/generated/jni-headers/qezu")))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
	void attachesJniHeaderDirectoryToObjectiveCHeaders() {
		subject.getSources().get(); // force realize until named can realize
		assertThat(subject.getSources().named("objectiveC", ObjectiveCSourceSetSpec.class).get().getHeaders().getSourceDirectories(),
			hasItem(aFile(withAbsolutePath(endsWith("/generated/jni-headers/qezu")))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
	void attachesJniHeaderDirectoryToObjectiveCppHeaders() {
		subject.getSources().get(); // force realize until named can realize
		assertThat(subject.getSources().named("objectiveCpp", ObjectiveCppSourceSetSpec.class).get().getHeaders().getSourceDirectories(),
			hasItem(aFile(withAbsolutePath(endsWith("/generated/jni-headers/qezu")))));
	}

	@Nested
	class ApiElementsConfigurationTest {
		public Configuration subject() {
			val result = project.getConfigurations().getByName("qezuApiElements");
			((ConfigurationInternal) result).preventFromFurtherMutation(); // force realization
			return result;
		}

		@Test
		void hasJvmJarBinaryAsOutgoingArtifact() {
			assertThat(subject(), ConfigurationMatchers.hasPublishArtifact(ofFile(withAbsolutePath(endsWith("/build/libs/qezu.jar")))));
		}
	}

	@Nested
	class RuntimeElementsConfigurationTest {
		public Configuration subject() {
			return project.getConfigurations().getByName("qezuRuntimeElements");
		}

		@Test
		void hasJvmJarBinaryAsOutgoingArtifact() {
			assertThat(subject(), ConfigurationMatchers.hasPublishArtifact(ofFile(withAbsolutePath(endsWith("/build/libs/qezu.jar")))));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return project.getTasks().getByName("assembleQezu");
		}

		@Test
		void dependsOnJvmJarBinary() {
			assertThat(subject(), TaskMatchers.dependsOn(hasItem(allOf(named("jarQezu"), isA(Jar.class)))));
		}
	}
}
