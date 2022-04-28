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
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.TaskMatchers.dependsOn;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentIntegrationTest.realize;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
class JavaNativeInterfaceLibraryVariantObjectiveCppLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("xapi", ProjectIdentifier.of(project));
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).withComponent(new FullyQualifiedNameComponent("xapi")).build());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryVariantRegistrationFactory.class);
		val variantIdentifier = VariantIdentifier.of(DefaultBuildVariant.of(of("linux-x64")), Variant.class, componentIdentifier);
		subject = registry.register(factory.create(variantIdentifier)).as(JniLibrary.class).get();
	}

	@Test
	void hasObjectiveCppSourceSetWhenObjectiveCppLanguagePluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("xapiLinuxX64ObjectiveCpp"), isA(ObjectiveCppSourceSet.class))));
	}

	@Nested
	class ObjectsTaskTest {
		public Task subject() {
			return project.getTasks().getByName("objectsXapiLinuxX64");
		}

		@Test
		void dependsOnObjectiveCppCompileTask() {
			assertThat(subject(), dependsOn(hasItem(allOf(named("compileXapiLinuxX64ObjectiveCpp"), isA(ObjectiveCppCompile.class)))));
		}
	}

	@Nested
	class NativeCompileOnlyConfigurationTest implements NativeCompileOnlyConfigurationTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("xapiLinuxX64NativeCompileOnly");
		}

		@Override
		public String displayName() {
			return "variant ':xapi:linuxX64'";
		}
	}

	@Nested
	class HeaderSearchPathsConfigurationTest implements NativeHeaderSearchPathsConfigurationTester {
		public Configuration subject() {
			return realize(project.getConfigurations().getByName("xapiLinuxX64ObjectiveCppHeaderSearchPaths"));
		}

		@Override
		public String displayName() {
			return "Objective-C++ sources ':xapi:linuxX64:sharedLibrary:objectiveCpp'";
		}

		@Override
		public String implementationConfigurationName() {
			return "xapiLinuxX64NativeCompileOnly";
		}

		@Override
		public String compileOnlyConfigurationName() {
			return "xapiLinuxX64NativeImplementation";
		}
	}

	@Nested
	class CompileTaskTest {
		public ObjectiveCppCompileTask subject() {
			return (ObjectiveCppCompileTask) project.getTasks().getByName("compileXapiLinuxX64ObjectiveCpp");
		}

		@Test
		void hasTargetPlatform() {
			assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86-64")));
		}
	}

	@Nested
	class ObjectiveCppSourceSetTest {
		public ObjectiveCppSourceSet subject() {
			return (ObjectiveCppSourceSet) subject.getSources().get().stream().filter(it -> it.getName().equals("xapiLinuxX64ObjectiveCpp")).collect(onlyElement());
		}

		@Test
		void usesConventionalSourceLocation() {
			assertThat(subject().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/xapi/objectiveCpp")))));
		}

		@Test
		void usesLegacyConventionalSourceLocation() {
			assertThat(subject().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/xapi/objcpp")))));
		}

		@Test
		void usesConventionalHeadersLocation() {
			assertThat(((HasHeaders) subject()).getHeaders().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/xapi/headers")))));
		}
	}
}
