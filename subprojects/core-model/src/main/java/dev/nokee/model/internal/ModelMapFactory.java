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
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;

public final class ModelMapFactory {
	private final ObjectFactory objects;
	private final Project project;
	private final DefaultKnownElements.Factory knownElementsFactory;
	private final ModelObjects modelObjects;
	private final ProviderFactory providers;
	private final DiscoveredElements discoveredElements;

	public ModelMapFactory(ObjectFactory objects, Project project, DefaultKnownElements.Factory knownElementsFactory, ModelObjects modelObjects, ProviderFactory providers, DiscoveredElements discoveredElements) {
		this.objects = objects;
		this.project = project;
		this.knownElementsFactory = knownElementsFactory;
		this.modelObjects = modelObjects;
		this.providers = providers;
		this.discoveredElements = discoveredElements;

		val container = objects.namedDomainObjectSet(Project.class);
		val knownElements = knownElementsFactory.create(new ModelMapAdapters.ModelElementIdentity.ElementProvider(container));
		container.configureEach(new InjectModelElementAction<>(it -> it.getName(), knownElements));
		modelObjects.register(objects.newInstance(ModelMapAdapters.ForProject.class, container, project, knownElements, discoveredElements));
	}

	public ModelMapAdapters.ForPolymorphicDomainObjectContainer<Task> create(TaskContainer container) {
		final Namer<Task> namer = new Task.Namer();
		val knownElements = knownElementsFactory.create(new ModelMapAdapters.ModelElementIdentity.ElementProvider(container));

		container.configureEach(new InjectModelElementAction<>(namer, knownElements));

		val result = objects.newInstance(ModelMapAdapters.ForTaskContainer.class, container, knownElements, discoveredElements, project);
		modelObjects.register(result);
		return result;
	}

	public ModelMapAdapters.ForNamedDomainObjectContainer<Configuration> create(ConfigurationContainer container) {
		final Namer<Configuration> namer = new Configuration.Namer();
		val knownElements = knownElementsFactory.create(new ModelMapAdapters.ModelElementIdentity.ElementProvider(container));

		container.configureEach(new InjectModelElementAction<>(namer, knownElements));

		val result = objects.newInstance(ModelMapAdapters.ForConfigurationContainer.class, container, knownElements, discoveredElements, project);
		modelObjects.register(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Named> ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T> create(Class<T> elementType, ExtensiblePolymorphicDomainObjectContainer<T> container) {
		final Namer<Named> namer = new Named.Namer();
		val knownElements = knownElementsFactory.create(new ModelMapAdapters.ModelElementIdentity.ElementProvider(container));

		container.configureEach(new InjectModelElementAction<>(namer, knownElements));

		val result = (ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer<T>) objects.newInstance(ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, elementType, container, instantiator(project), knownElements, knownElements, discoveredElements, new ModelMapAdapters.ContextualModelElementInstantiator() {
			@Override
			public <S> Function<DefaultKnownElements.KnownElement, S> newInstance(Factory<S> factory) {
				return element -> ModelElementSupport.newInstance(create(element), factory);
			}
		}, project);
		modelObjects.register(result);
		return result;
	}

	private ModelElement create(DefaultKnownElements.KnownElement element) {
		return new ModelElement() {
			@Override
			public ModelObjectIdentifier getIdentifier() {
				return element.getIdentifier();
			}

			@Override
			public Stream<ModelElement> getParents() {
				return modelObjects.parentsOf(element.getIdentifier()).map(it -> it.asProvider().map(ModelElementSupport::asModelElement).get());
			}

			@Override
			public boolean instanceOf(Class<?> type) {
				return element.identifiers.stream().anyMatch(it -> it.instanceOf(type));
			}

			@Override
			public <T> Provider<T> safeAs(Class<T> type) {
				return providers.provider(() -> element.identifiers.stream().filter(it -> it.instanceOf(type)).map(it -> it.asModelObject(type).asProvider()).findFirst().orElse(null)).flatMap(it -> it);
			}

			@Override
			public String getName() {
				return element.getName();
			}

			@Override
			public String toString() {
				return element.toString();
			}
		};
	}

	private final class InjectModelElementAction<S> implements Action<S> {
		private final Namer<S> namer;
		private final DefaultKnownElements knownElements;

		private InjectModelElementAction(Namer<S> namer, DefaultKnownElements knownElements) {
			this.namer = namer;
			this.knownElements = knownElements;
		}

		@Override
		public void execute(S it) {
			final DefaultKnownElements.KnownElement element = knownElements.findByName(namer.determineName(it));
			if (element != null) {
				ModelElementSupport.injectIdentifier(it, create(element));
			}
		}
	}
}
