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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasCompileTask;
import dev.nokee.language.base.internal.HasConfigurableSource;
import dev.nokee.language.base.tasks.SourceCompile;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

public interface LanguageSourceSetHasCompiledSourceIntegrationTester<T extends LanguageSourceSet & HasConfigurableSource & HasCompileTask> {
	T subject();

	@BeforeEach
	default void hasSourceCompileTask() {
		assertThat(subject().getCompileTask(), providerOf(isA(SourceCompile.class)));
	}

	@Test
	default void includesSourceSetFilesInCompileTaskSources() throws IOException {
		val path = Files.createTempFile("source", ".file").toFile();
		subject().getSource().from(path);
		assertThat(((SourceCompile) subject()).getSource(), hasItem(aFile(path)));
	}
}
