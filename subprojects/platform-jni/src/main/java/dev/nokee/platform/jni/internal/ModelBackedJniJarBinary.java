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

import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

@DomainObjectEntities.Tag({IsBinary.class, ConfigurableTag.class})
public /*final*/ class ModelBackedJniJarBinary implements JniJarBinary, ModelNodeAware, HasPublicType, ModelBackedNamedMixIn {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public TaskProvider<Jar> getJarTask() {
		return (TaskProvider<Jar>) ModelElements.of(this).element("jar", Jar.class).asProvider();
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
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(JniJarBinary.class);
	}
}
