/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal;

import dev.nokee.internal.Factory;
import lombok.val;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;

public final class ModelMapFactory {
	private final ObjectFactory objects;
	private final Project project;
	private final Factory<KnownElements> knownElementsFactory;
	private final ModelObjects modelObjects;

	public ModelMapFactory(ObjectFactory objects, Project project, Factory<KnownElements> knownElementsFactory, ModelObjects modelObjects) {
		this.objects = objects;
		this.project = project;
		this.knownElementsFactory = knownElementsFactory;
		this.modelObjects = modelObjects;
	}

	public ModelMapAdapters.ForPolymorphicDomainObjectContainer<Task> create(TaskContainer tasks) {
		val result = objects.newInstance(ModelMapAdapters.ForTaskContainer.class, tasks, project, knownElementsFactory);
		modelObjects.register(result);
		return result;
	}

	public ModelMapAdapters.ForNamedDomainObjectContainer<Configuration> create(ConfigurationContainer configurations) {
		val result = objects.newInstance(ModelMapAdapters.ForConfigurationContainer.class, configurations, project, knownElementsFactory);
		modelObjects.register(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Named> ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T> create(Class<T> elementType, ExtensiblePolymorphicDomainObjectContainer<T> container) {
		val result = (ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T>) objects.newInstance(ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, elementType, new Named.Namer(), container, instantiator(project), project, knownElementsFactory);
		modelObjects.register(result);
		return result;
	}
}
