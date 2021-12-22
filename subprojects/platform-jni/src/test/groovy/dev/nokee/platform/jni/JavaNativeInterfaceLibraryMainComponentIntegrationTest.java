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
import com.google.common.collect.MoreCollectors;
import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.internal.testing.FileSystemMatchers.containsPath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.TaskMatchers.dependsOn;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library")
class JavaNativeInterfaceLibraryMainComponentIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		this.subject = project.getExtensions().getByType(JavaNativeInterfaceLibrary.class);
	}

	@Test
	void usesProjectNameAsComponentBaseNameConvention() {
		subject.getBaseName().set((String) null);
		assertThat(subject.getBaseName(), providerOf(project.getName()));
	}

	@Nested
	class ResourcePathConventionTest {
		@Test
		void hasEmptyResourcePathWhenGroupIsEmptyAndSingleVariant() {
			project.setGroup("");
			assertThat(allResourcePaths(), contains(providerOf(emptyString())));
		}

		@Test
		void usesProjectGroupAsResourcePathOnlyWhenSingleVariant() {
			project.setGroup("com.example.hezebe");
			assertThat(allResourcePaths(), contains(providerOf("com/example/hezebe")));
		}

		@Test
		void usesProjectGroupAtStartOfResourcePathForAllVariant() {
			project.setGroup("com.example.reqawa");
			subject.getTargetMachines().set(ImmutableSet.of(of("windows-x86"), of("linux-x64")));
			assertThat(allResourcePaths(), contains(providerOf(startsWith("com/example/reqawa/")), providerOf(startsWith("com/example/reqawa/"))));
		}

		@Test
		void usesVariantAmbiguousDimensionAtEndOfResourcePathForAllVariant() {
			project.setGroup("com.example.xupude");
			subject.getTargetMachines().set(ImmutableSet.of(of("macos-x64"), of("linux-x64")));
			assertThat(allResourcePaths(), contains(providerOf(endsWith("/macos")), providerOf(endsWith("/linux"))));
		}

		@Test
		void includesAllVariantAmbiguousDimensionsAsKebabCaseInResourcePathForAllVariant() {
			project.setGroup("com.example.mijupa");
			subject.getTargetMachines().set(ImmutableSet.of(of("macos-x64"), of("windows-x86")));
			assertThat(allResourcePaths(), contains(providerOf(endsWith("/macos-x64")), providerOf(endsWith("/windows-x86"))));
		}

		@Test
		void canChangeProjectGroupAfterVariantRealize() {
			project.setGroup("com.example.zepacu");
			assertThat(allResourcePaths(), contains(providerOf("com/example/zepacu")));
			project.setGroup("com.example.mifohu");
			assertThat(allResourcePaths(), contains(providerOf("com/example/mifohu")));
		}

		private List<Provider<String>> allResourcePaths() {
			return subject.getVariants().get().stream().map(it -> it.getResourcePath().value((String) null)).collect(toList());
		}
	}

	@Nested
	@PluginRequirement.Require(id = "java")
	class WhenJavaPluginApplied {
		@Nested
		class TestTaskTest {
			public org.gradle.api.tasks.testing.Test subject() {
				return (org.gradle.api.tasks.testing.Test) project.getTasks().getByName("test");
			}

			@Test
			void doesNotHaveTaskDependenciesWhenNoDevelopmentVariant() {
				// Fake an unbuildable variant :-)
				subject.getDevelopmentVariant().convention((JniLibrary) null).value((JniLibrary) null);
				assertThat(subject(), dependsOn(emptyIterable()));
			}

			@Test
			void hasLinkTaskDependencyWhenDevelopmentVariantPresent() {
				assertThat(subject(), dependsOn(hasItem(allOf(named("link"), isA(LinkSharedLibrary.class)))));
			}

			@Test
			void hasDevelopmentVariantNativeRuntimeFilesParentDirectoryInJavaLibraryPathSystemProperty() {
				assertThat(subject().getAllJvmArgs(), hasItem(allOf(startsWith("-Djava.library.path="), containsPath("/build/libs/main"))));
			}
		}
	}
}
