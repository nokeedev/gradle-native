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

import dev.nokee.internal.testing.NativeServicesInitializedOnWindows;
import dev.nokee.language.base.tasks.SourceCompile;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.platform.base.ToolChain;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface SourceCompileTester extends HasDestinationDirectoryTester {
	SourceCompile subject();

	@Test
	@NativeServicesInitializedOnWindows
	default void hasToolChain() {
		assertThat("not null as per contract", subject().getToolChain(), notNullValue(Provider.class));
		assertThat("provide a ToolChain", subject().getToolChain(), providerOf(isA(ToolChain.class)));
	}

	@Test
	default void hasCompilerArgs() {
		assertThat("not null as per contract", subject().getCompilerArgs(), notNullValue(ListProperty.class));
		assertThat("there should be a value", subject().getCompilerArgs(), presentProvider());
	}

	@Test
	default void hasSource() {
		assertThat("not null as per contract", subject().getSource(), notNullValue(ConfigurableFileCollection.class));
	}
}
