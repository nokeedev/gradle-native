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
package dev.nokee.platform.nativebase.testers;

import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.testers.ArtifactTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface SharedLibraryBinaryTester extends ArtifactTester<SharedLibraryBinary>, HasBaseNameTester {
	@Test
	default void hasCompileTasks() {
		assertThat(subject().getCompileTasks(), notNullValue(TaskView.class));
	}

	@Test
	default void hasLinkTask() {
		assertThat(subject().getLinkTask(), providerOf(isA(LinkSharedLibrary.class)));
	}

	@Test
	default void includesLinkTaskAsBuildable() {
		assertThat(subject(), buildDependencies(hasItem(subject().getLinkTask().get())));
	}

	@Test
	default void doesNotThrowAnyExceptionWhenCheckingBuildability() {
		assertDoesNotThrow(() -> subject().isBuildable());
	}
}
