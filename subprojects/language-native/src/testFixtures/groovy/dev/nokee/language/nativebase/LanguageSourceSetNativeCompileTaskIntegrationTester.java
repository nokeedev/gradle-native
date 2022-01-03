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

import dev.nokee.language.base.HasDestinationDirectory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasCompileTask;
import dev.nokee.language.base.tasks.SourceCompile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;

public interface LanguageSourceSetNativeCompileTaskIntegrationTester<T extends LanguageSourceSet & HasCompileTask> {
	T subject();

	@BeforeEach
	default void hasDestinationDirectorySourceCompileTask() {
		// Use SourceCompile because of SwiftCompile is not NativeSourceCompile
		assertThat(subject().getCompileTask(),
			providerOf(allOf(isA(SourceCompile.class), isA(HasDestinationDirectory.class))));
	}

	@Test
	default void includesLanguageSourceSetNameInDestinationDirectory() {
		assertThat(((SourceCompile) subject().getCompileTask().get()).getDestinationDirectory(),
			providerOf(aFileNamed(subject().getName())));
	}
}
