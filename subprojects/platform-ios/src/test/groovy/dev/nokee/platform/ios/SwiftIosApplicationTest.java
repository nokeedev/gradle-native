/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.ios;

import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.swift.HasSwiftSourcesTester;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.testers.BinaryAwareComponentTester;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.TaskAwareComponentTester;
import dev.nokee.platform.base.testers.VariantAwareComponentTester;
import dev.nokee.platform.base.testers.VariantDimensionsIntegrationTester;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundle;
import dev.nokee.platform.ios.internal.plugins.IosComponentBasePlugin;
import dev.nokee.platform.ios.internal.plugins.IosResourcePlugin;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.MapProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.ios.internal.plugins.SwiftIosApplicationPlugin.swiftIosApplication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;

class SwiftIosApplicationTest implements ComponentTester<SwiftIosApplication>
	, HasBaseNameTester
	, DependencyAwareComponentTester<NativeComponentDependencies>
	, VariantAwareComponentTester<VariantView<NativeApplication>>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
	, HasSwiftSourcesTester
{
	private SwiftIosApplication subject;
	@Getter @TempDir File testDirectory;

	@BeforeEach
	void createASubject() {
		subject = createSubject("foma");
	}

	public SwiftIosApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(IosComponentBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);
		project.getPluginManager().apply(IosResourcePlugin.class);
		val component = project.getExtensions().getByType(ModelRegistry.class).register(swiftIosApplication(componentName, project)).as(SwiftIosApplication.class).get();
		return component;
	}

	@Override
	public SwiftIosApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("foma"));
	}

	@Nested
	class ComponentTasksTest {
		public TaskView<Task> subject() {
			return subject.getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assembleFoma")));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return subject.getTasks().filter(it -> it.getName().equals("assembleFoma")).get().get(0);
		}

		@Test
		public void hasBuildGroup() {
			assertThat(subject(), TaskMatchers.group("build"));
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of the Swift iOS application ':foma'."));
		}
	}

	@Nested
	class ComponentDependenciesTest {
		public NativeComponentDependencies subject() {
			return subject.getDependencies();
		}

		@Test
		void hasImplementation() {
			assertThat(subject().getImplementation().getAsConfiguration(), named("fomaImplementation"));
		}

		@Test
		void hasCompileOnly() {
			assertThat(subject().getCompileOnly().getAsConfiguration(), named("fomaCompileOnly"));
		}

		@Test
		void hasLinkOnly() {
			assertThat(subject().getLinkOnly().getAsConfiguration(), named("fomaLinkOnly"));
		}

		@Test
		void hasRuntimeOnly() {
			assertThat(subject().getRuntimeOnly().getAsConfiguration(), named("fomaRuntimeOnly"));
		}
	}

	@Nested
	class ImplementationConfigurationTest {
		public Configuration subject() {
			return subject.getDependencies().getImplementation().getAsConfiguration();
		}

		@Test
		public void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description(startsWith("Implementation dependencies for ")));
		}
	}

	@Nested
	class CompileOnlyConfigurationTest {
		public Configuration subject() {
			return subject.getDependencies().getCompileOnly().getAsConfiguration();
		}

		@Test
		public void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description(startsWith("Compile only dependencies for ")));
		}
	}

	@Nested
	class LinkOnlyConfigurationTest {
		public Configuration subject() {
			return subject.getDependencies().getLinkOnly().getAsConfiguration();
		}

		@Test
		public void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description(startsWith("Link only dependencies for ")));
		}
	}

	@Nested
	class RuntimeOnlyConfigurationTest {
		public Configuration subject() {
			return subject.getDependencies().getRuntimeOnly().getAsConfiguration();
		}

		@Test
		public void isDeclarable() {
			assertThat(subject(), ConfigurationMatchers.declarable());
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description(startsWith("Runtime only dependencies for ")));
		}
	}

	@Nested
	class ComponentBinariesTest {
		public BinaryView<Binary> subject() {
			return subject.getBinaries();
		}

		@Test
		void hasExecutableBinary() {
			assertThat(subject().get(), hasItem(isA(ExecutableBinary.class)));
		}

		@Test
		void hasApplicationBundle() {
			assertThat(subject().get(), hasItem(isA(IosApplicationBundleInternal.class)));
		}

		@Test
		void hasSignedApplicationBundle() {
			assertThat(subject().get(), hasItem(isA(SignedIosApplicationBundle.class)));
		}
	}

	@Nested
	class ComponentVariantsTest {
		public VariantView<IosApplication> subject() {
			return subject.getVariants();
		}

		@Nested
		class SingleVariantTest {
			private IosApplication variant;

			@BeforeEach
			void singleVariant() {
				variant = subject.getVariants().get().iterator().next();
			}

			@Nested
			class ComponentDependenciesTest {
				public NativeComponentDependencies subject() {
					return variant.getDependencies();
				}

				@Test
				@SuppressWarnings("unchecked")
				public void hasLinkLibraries() {
					assertThat(((MapProperty<String, DependencyBucket>) ModelNodes.of(subject()).get(GradlePropertyComponent.class).get()).getting("linkLibraries").map(DependencyBucket::getAsConfiguration), providerOf(named("fomaLinkLibraries")));
				}
			}
		}
	}

	@Nested
	class VariantDimensionsTest extends VariantDimensionsIntegrationTester {
		@Override
		public VariantAwareComponent<?> subject() {
			return subject;
		}
	}
}
