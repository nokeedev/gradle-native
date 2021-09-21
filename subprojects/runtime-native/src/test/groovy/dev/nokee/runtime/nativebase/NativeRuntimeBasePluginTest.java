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
package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.base.internal.RuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NativeRuntimeBasePluginTest {
	private static Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply(NativeRuntimeBasePlugin.class);
		return project;
	}

	@Test
	void appliesRuntimeBasePlugin() {
		assertThat(createSubject().getPlugins(), hasItem(isA(RuntimeBasePlugin.class)));
	}

	@Test
	void registerOperatingSystemFamilyAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
	}

	@Test
	void registerMachineArchitectureAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
	}

	@Test
	void registerBuildTypeAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(BuildType.BUILD_TYPE_ATTRIBUTE)));
	}

	@Test
	void registerBinaryLinkageAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE)));
	}
}
