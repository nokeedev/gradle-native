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
package dev.nokee.platform.jni.internal;

import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;

public /*final*/ abstract class ModelBackedJniJarBinary extends ModelElementSupport implements JniJarBinary {
	@Inject
	public ModelBackedJniJarBinary(ModelObjectRegistry<Task> taskRegistry) {
		getExtensions().add("jarTask", taskRegistry.register(getIdentifier().child(TaskName.of("jar")), Jar.class).asProvider());
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskProvider<Jar> getJarTask() {
		return (TaskProvider<Jar>) getExtensions().getByName("jarTask");
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getJarTask());
	}

	@Override
	public String toString() {
		return "JNI JAR binary '" + getName() + "'";
	}
}
