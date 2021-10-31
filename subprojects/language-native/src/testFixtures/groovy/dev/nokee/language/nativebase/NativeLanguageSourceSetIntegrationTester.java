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
package dev.nokee.language.nativebase;

import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.testers.ConfigurableSourceSetIntegrationTester;
import dev.nokee.language.base.testers.LanguageSourceSetIntegrationTester;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.core.ModelProperties;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createDependency;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public abstract class NativeLanguageSourceSetIntegrationTester<T extends LanguageSourceSet> extends LanguageSourceSetIntegrationTester<T> {
	public abstract T subject();

	public abstract Project project();

	public abstract String name();

	public abstract String variantName();

	public abstract String displayName();

	private Configuration headerSearchPaths() {
		return project().getConfigurations().getByName(variantName() + "HeaderSearchPaths");
	}

	private ConfigurableSourceSet headers() {
		return ModelProperties.getProperty(subject(), "headers")
			.as(ConfigurableSourceSet.class).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void includesHeadersBuildDependenciesInSourceSetBuildDependencies() {
		val anyTask = mock(Task.class);
		val buildTask = project().getTasks().create("buildSomeHeaders");
		headers().from(project().getObjects().fileCollection().from("foo.txt").builtBy(buildTask));
		assertThat((Set<Task>) subject().getBuildDependencies().getDependencies(anyTask), hasItem(buildTask));
	}

	@Nested
	class NativeCompileTaskTest implements NativeCompileTaskTester, NativeCompileTaskObjectFilesTester<NativeSourceCompile> {
		public NativeSourceCompile subject() {
			return ModelProperties.getProperty(NativeLanguageSourceSetIntegrationTester.this.subject(), "compileTask")
				.as(NativeSourceCompile.class).get();
		}

		@Override
		public String languageSourceSetName() {
			return name();
		}

		@Test
		void isNativeSourceCompile() {
			assertThat(subject(), isA(NativeSourceCompile.class));
		}

		@Test
		void disablesPositionIndependentCode() {
			assertThat(((AbstractNativeCompileTask) subject()).isPositionIndependentCode(), is(false));
		}

		@Test
		void linksHeaderSourcePathsConfigurationToCompileTaskIncludes() throws IOException {
			val hdrs = Files.createTempDirectory("hdrs").toFile();
			headerSearchPaths().getDependencies().add(createDependency(objectFactory().fileCollection().from(hdrs)));
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(hdrs))));
		}

		@Test
		void includesSourceSetHeaderFilesInCompileTaskHeaders() throws IOException {
			val path = Files.createTempDirectory("headers").toFile();
			headers().from(path);
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(path))));
		}
	}

	@Nested
	class HeadersPropertyTest extends ConfigurableSourceSetIntegrationTester {
		@Override
		public ConfigurableSourceSet subject() {
			return headers();
		}

		@Override
		public File getTemporaryDirectory() throws IOException {
			return Files.createDirectories(project().getProjectDir().toPath()).toFile();
		}
	}

	@Nested
	class HeaderSearchPathsConfigurationTest {
		public Configuration subject() {
			return headerSearchPaths();
		}

		@Test
		void isResolvable() {
			assertThat(subject(), ConfigurationMatchers.resolvable());
		}

		@Test
		void hasCPlusPlusApiUsage() {
			assertThat(subject(), ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("cplusplus-api"))));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Header search paths for " + displayName() + "."));
		}
	}
}
