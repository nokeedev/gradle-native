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

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
class SharedLibraryBinarySpecBuildabilityIntegrationTest extends NativeBinaryBuildabilityIntegrationTester<SharedLibraryBinaryRegistrationFactory.ModelBackedSharedLibraryBinary> {
	public SharedLibraryBinaryRegistrationFactory.ModelBackedSharedLibraryBinary createSubject() {
		val factory = project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		return registry.register(factory.create(projectIdentifier.child("jecu"))).as(SharedLibraryBinaryRegistrationFactory.ModelBackedSharedLibraryBinary.class).get();
	}
}
