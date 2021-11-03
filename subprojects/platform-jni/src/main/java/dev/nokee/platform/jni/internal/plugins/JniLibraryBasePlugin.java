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
package dev.nokee.platform.jni.internal.plugins;

import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.jni.internal.JarTaskRegistrationActionFactory;
import dev.nokee.platform.jni.internal.JniJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.JvmJarBinaryRegistrationFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JniLibraryBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val jarTaskRegistrationFactory = new JarTaskRegistrationActionFactory(
			() -> project.getExtensions().getByType(TaskRegistrationFactory.class),
			() -> project.getExtensions().getByType(ModelRegistry.class),
			() -> project.getExtensions().getByType(ModelPropertyRegistrationFactory.class)
		);
		project.getExtensions().add("__nokee_jniJarBinaryFactory", new JniJarBinaryRegistrationFactory(jarTaskRegistrationFactory));
		project.getExtensions().add("__nokee_jvmJarBinaryFactory", new JvmJarBinaryRegistrationFactory(jarTaskRegistrationFactory));
	}
}
