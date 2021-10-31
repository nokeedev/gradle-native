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
package dev.nokee.language.base.testers;

import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.core.ModelProperties;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public abstract class LanguageSourceSetIntegrationTester<T extends LanguageSourceSet> {
	public abstract T subject();

	public abstract Project project();

	public abstract String variantName();

	public abstract String displayName();

	private ConfigurableSourceSet source() {
		return ModelProperties.getProperty(subject(), "source").as(ConfigurableSourceSet.class).get();
	}

	@Nested
	class CompileTaskPropertyTest {
		public TaskProvider<? extends SourceCompile> subject() {
			return ModelProperties.getProperty(LanguageSourceSetIntegrationTester.this.subject(), "compileTask")
				.as(TaskProvider.class).get();
		}

		@Test
		void hasCompileTaskWithProperName() {
			assertThat(subject(), providerOf(named("compile" + capitalize(variantName()))));
		}
	}

	@Nested
	class SourcePropertyTest extends ConfigurableSourceSetIntegrationTester {
		@Override
		public ConfigurableSourceSet subject() {
			return source();
		}

		@Override
		public File getTemporaryDirectory() throws IOException {
			return Files.createDirectories(project().getProjectDir().toPath()).toFile();
		}
	}

	@Nested
	class BaseCompileTaskTest {
		public SourceCompile subject() {
			return (SourceCompile) project().getTasks().getByName("compile" + capitalize(variantName()));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Compiles the " + displayName() + "."));
		}

		@Test
		void includesSourceSetFilesInCompileTaskSources() throws IOException {
			val path = Files.createTempFile("source", ".file").toFile();
			source().from(path);
			assertThat(subject().getSource(), hasItem(aFile(path)));
		}
	}
}
