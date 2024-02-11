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
import dev.nokee.internal.reflect.Instantiator;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

public final class ModelMapFactory {
	private final Instantiator instantiator;
	private final ModelObjects modelObjects;

	public ModelMapFactory(Instantiator instantiator, ObjectFactory objects, Project project, ModelObjects modelObjects) {
		this.instantiator = instantiator;
		this.modelObjects = modelObjects;

		register(() -> {
			final NamedDomainObjectSet<Project> container = objects.namedDomainObjectSet(Project.class);
			return instantiator.newInstance(ModelMapAdapters.ForProject.class, container, project);
		});
	}

	public ModelMapAdapters.ForPolymorphicDomainObjectContainer<Task> create(TaskContainer container) {
		return register(() -> instantiator.newInstance(ModelMapAdapters.ForTaskContainer.class, container));
	}

	public ModelMapAdapters.ForNamedDomainObjectContainer<Configuration> create(ConfigurationContainer container) {
		return register(() -> instantiator.newInstance(ModelMapAdapters.ForConfigurationContainer.class, container));
	}

	@SuppressWarnings("unchecked")
	public <T extends Named> ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T> create(Class<T> elementType, ExtensiblePolymorphicDomainObjectContainer<T> container) {
		return register(() -> (ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T>) instantiator.newInstance(ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, elementType, container));
	}

	private <R extends ModelMap<T>, T> R register(Factory<R> action) {
		final R result = action.create();
		modelObjects.register(result);
		return result;
	}
}
