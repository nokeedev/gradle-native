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
package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.RuntimeBasePlugin;
import dev.nokee.runtime.nativebase.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributesSchema;

public /*final*/ abstract class NativeRuntimeBasePlugin implements Plugin<Project> {
	public static final TargetMachineFactory TARGET_MACHINE_FACTORY = new DefaultTargetMachineFactory();
	public static final TargetLinkageFactory TARGET_LINKAGE_FACTORY = new DefaultTargetLinkageFactory();
	public static final TargetBuildTypeFactory TARGET_BUILD_TYPE_FACTORY = new DefaultTargetBuildTypeFactory();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(RuntimeBasePlugin.class);
		project.getDependencies().attributesSchema(this::registerNativeAttributes);
	}
	private void registerNativeAttributes(AttributesSchema schema) {
		schema.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE);
		schema.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE);
		schema.attribute(BuildType.BUILD_TYPE_ATTRIBUTE);
		schema.attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE);
	}
}
