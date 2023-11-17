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
package dev.nokee.platform.nativebase;

import dev.nokee.internal.testing.IntegrationTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.aFileBaseNamed;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.artifacts;
import static dev.nokee.platform.nativebase.NativePlatformTestUtils.macosPlatform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@IntegrationTest
class ExecutableBinarySpecBaseNameIntegrationTest implements HasBaseNameTester {
	@GradleProject Project project;
	ExecutableBinary subject;

	@BeforeEach
	void createSubject() {
		subject = artifacts(project).register("wepo", ExecutableBinaryInternal.class).get();
		subject.getLinkTask().configure(task -> ((LinkExecutableTask) task).getTargetPlatform().set(macosPlatform()));
	}

	@Override
	public HasBaseName subject() {
		return subject;
	}

	@Test
	void usesBinaryBaseNameForLinkTaskLinkedFileBaseName() {
		subject.getBaseName().set("da-bo");
		assertThat(subject.getLinkTask().flatMap(LinkExecutable::getLinkedFile),
			providerOf(aFileBaseNamed(endsWith("da-bo"))));
	}

	@Test
	void usesBinaryNameAsBaseNameByDefault() {
		subject.getBaseName().set((String) null); // force convention
		assertThat(subject.getBaseName(), providerOf("wepo"));
	}
}
