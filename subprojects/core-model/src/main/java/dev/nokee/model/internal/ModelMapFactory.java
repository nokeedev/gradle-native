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
import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Namer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import java.util.function.Function;

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

	public ModelMapAdapters.ForPolymorphicDomainObjectContainer<Task> create(TaskContainer container) {
		final Namer<Task> namer = new Task.Namer();
		val knownElements = knownElementsFactory.create();

		container.configureEach(knownElements.forCreatedElements(namer, container::named));
		container.configureEach(new InjectModelElementAction<>(namer, knownElements));

		val result = objects.newInstance(ModelMapAdapters.ForTaskContainer.class, container, project, knownElements);
		modelObjects.register(result);
		return result;
	}

	public ModelMapAdapters.ForNamedDomainObjectContainer<Configuration> create(ConfigurationContainer container) {
		final Namer<Configuration> namer = new Configuration.Namer();
		val knownElements = knownElementsFactory.create();

		container.configureEach(knownElements.forCreatedElements(namer, container::named));
		container.configureEach(new InjectModelElementAction<>(namer, knownElements));

		val result = objects.newInstance(ModelMapAdapters.ForConfigurationContainer.class, container, project, knownElements);
		modelObjects.register(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Named> ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T> create(Class<T> elementType, ExtensiblePolymorphicDomainObjectContainer<T> container) {
		final Namer<Named> namer = new Named.Namer();
		val knownElements = knownElementsFactory.create();

		container.configureEach(knownElements.forCreatedElements(namer, container::named));
		container.configureEach(new InjectModelElementAction<>(namer, knownElements));

		val result = (ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T>) objects.newInstance(ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, elementType, container, instantiator(project), project, knownElements, new ModelMapAdapters.ContextualModelElementInstantiator() {
			@Override
			public <S> Function<KnownElements.KnownElement, S> newInstance(Factory<S> factory) {
				return element -> {
					return ModelElementSupport.newInstance(new ModelElement() {
						@Override
						public ModelObjectIdentifier getIdentifier() {
							return element.getIdentifier();
						}

						@Override
						public String getName() {
							return element.getName();
						}
					}, factory);
				};
			}
		});
		modelObjects.register(result);
		return result;
	}

	private static final class InjectModelElementAction<S> implements Action<S> {
		private final Namer<S> namer;
		private final KnownElements knownElements;

		private InjectModelElementAction(Namer<S> namer, KnownElements knownElements) {
			this.namer = namer;
			this.knownElements = knownElements;
		}

		@Override
		public void execute(S it) {
			final KnownElements.KnownElement element = knownElements.findByName(namer.determineName(it));
			if (element != null) {
				ModelElementSupport.injectIdentifier(it, new ModelElement() {
					@Override
					public ModelObjectIdentifier getIdentifier() {
						return element.getIdentifier();
					}

					@Override
					public String getName() {
						return element.getName();
					}
				});
			}
		}
	}
}
