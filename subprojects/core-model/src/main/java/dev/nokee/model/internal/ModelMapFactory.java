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

import lombok.val;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import java.util.stream.Stream;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;

public final class ModelMapFactory {
	private final ObjectFactory objects;
	private final Project project;
	private final ModelObjects modelObjects;
	private final DiscoveredElements discoveredElements;
	private final ModelElementFinalizer finalizer;
	private final ModelMapAdapters.ModelElementParents elementParents = new ModelMapAdapters.ModelElementParents() {
		@Override
		public Stream<ModelElement> parentOf(ModelObjectIdentifier identifier) {
			return modelObjects.parentsOf(identifier).map(it -> it.asProvider().map(ModelElementSupport::asModelElement).get());
		}
	};

	public ModelMapFactory(ObjectFactory objects, Project project, ModelObjects modelObjects, DiscoveredElements discoveredElements, ModelElementFinalizer finalizer) {
		this.objects = objects;
		this.project = project;
		this.modelObjects = modelObjects;
		this.discoveredElements = discoveredElements;
		this.finalizer = finalizer;

		val container = objects.namedDomainObjectSet(Project.class);
		modelObjects.register(objects.newInstance(ModelMapAdapters.ForProject.class, container, project, discoveredElements, finalizer, elementParents));
	}

	public ModelMapAdapters.ForPolymorphicDomainObjectContainer<Task> create(TaskContainer container) {
		val result = objects.newInstance(ModelMapAdapters.ForTaskContainer.class, container, discoveredElements, project, finalizer, elementParents);
		modelObjects.register(result);
		return result;
	}

	public ModelMapAdapters.ForNamedDomainObjectContainer<Configuration> create(ConfigurationContainer container) {
		val result = objects.newInstance(ModelMapAdapters.ForConfigurationContainer.class, container, discoveredElements, project, finalizer, elementParents);
		modelObjects.register(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Named> ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T> create(Class<T> elementType, ExtensiblePolymorphicDomainObjectContainer<T> container) {
		val result = (ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T>) objects.newInstance(ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, elementType, container, instantiator(project), discoveredElements, project, finalizer, elementParents);
		modelObjects.register(result);
		return result;
	}
}
