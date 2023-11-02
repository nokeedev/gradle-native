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
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;

@DomainObjectEntities.Tag({IsBinary.class, ConfigurableTag.class})
public /*final*/ abstract class ModelBackedJvmJarBinary extends ModelElementSupport implements JvmJarBinary, Buildable, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public ModelBackedJvmJarBinary(ModelObjectRegistry<Task> taskRegistry) {
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
	public ModelNode getNode() {
		return node;
	}

	@Override
	public String toString() {
		return "JVM JAR binary '" + getName() + "'";
	}
}
