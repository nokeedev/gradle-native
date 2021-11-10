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

import dev.nokee.internal.testing.testers.ConfigureMethodTester;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.testers.BinaryAwareComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.TaskAwareComponentTester;
import dev.nokee.platform.base.testers.VariantTester;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public interface JavaNativeInterfaceLibraryVariantTester extends VariantTester<JniLibrary>
	, DependencyAwareComponentTester<JavaNativeInterfaceNativeComponentDependencies>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
{
	@Test
	default void hasResourcePath() {
		assertThat(subject().getResourcePath(), notNullValue());
	}

	@Test
	default void hasSharedLibrary() {
		assertThat(subject().getSharedLibrary(), notNullValue());
	}

	@Test
	default void canConfigureSharedLibrary() {
		ConfigureMethodTester.of(subject(), JniLibrary::getSharedLibrary)
			.testAction(JniLibrary::sharedLibrary)
			.testClosure(JniLibrary::sharedLibrary);
	}

	@Test
	default void hasTargetMachine() {
		assertThat(subject().getTargetMachine(), notNullValue());
	}

	@Test
	default void hasNativeRuntimeFiles() {
		assertThat(subject().getNativeRuntimeFiles(), notNullValue());
	}
}
