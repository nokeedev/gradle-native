/*
 * Copyright 2022 the original author or authors.
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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasCompileTask;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.*;
import static dev.nokee.utils.ConfigurationUtils.*;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface LanguageSourceSetHasCompiledHeaderSearchPathsIntegrationTester<T extends LanguageSourceSet & HasCompileTask> {
	T subject();

	Configuration headerSearchPaths();

	@BeforeEach
	default void hasNativeSourceCompileTaskForHeaderSearchPaths() {
		assertThat(subject().getCompileTask(), providerOf(isA(NativeSourceCompile.class)));
	}

	@Test
	default void linksHeaderSourcePathsConfigurationToCompileTaskIncludes() throws IOException {
		val hdrs = Files.createTempDirectory("hdrs").toFile();
		headerSearchPaths().getDependencies().add(createDependency(objectFactory().fileCollection().from(hdrs)));
		assertThat(((NativeSourceCompile) subject().getCompileTask().get()).getHeaderSearchPaths(),
			providerOf(hasItem(aFile(hdrs))));
	}

	@Test
	default void linksHeaderSourcePathsConfigurationToCompileTaskAsFrameworkCompileArguments() throws IOException {
		val artifact = Files.createTempDirectory("Kuqo.framework").toFile();
		val frameworkProducer = rootProject();
		frameworkProducer.getConfigurations().create("apiElements",
			configureAsConsumable()
				.andThen(configureAttributes(forUsage(objectFactory().named(Usage.class, Usage.C_PLUS_PLUS_API))))
				.andThen(configureAttributes(it -> it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
					objectFactory().named(LibraryElements.class, "framework-bundle"))))
				.andThen(it -> it.getOutgoing().artifact(artifact, t -> t.setType("framework")))
		);

		headerSearchPaths().getDependencies().add(createDependency(frameworkProducer));
		assertThat(((NativeSourceCompile) subject().getCompileTask().get()).getHeaderSearchPaths(),
			providerOf(allOf(not(hasItem(aFile(artifact))), not(hasItem(aFile(artifact.getParentFile()))))));
		assertThat(((NativeSourceCompile) subject().getCompileTask().get()).getCompilerArgs(),
			providerOf(containsInRelativeOrder("-F", artifact.getParentFile().getAbsolutePath())));
	}
}
