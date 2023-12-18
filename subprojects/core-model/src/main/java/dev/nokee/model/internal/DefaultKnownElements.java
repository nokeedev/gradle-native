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

import dev.nokee.model.PolymorphicDomainObjectRegistry;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.ModelObjectIdentity.ofIdentity;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;

public final class DefaultKnownElements implements KnownElements {
	private final Set<ModelObjectIdentifier> knownIdentifiers = new HashSet<>();
	private final DomainObjectSet<RealizableElement> realizableElements;
	private final ProjectIdentifier projectIdentifier;
	private final DomainObjectSet<ModelMapAdapters.ModelElementIdentity> knownElements;
	private final NamedDomainObjectSet<KnownElement> mapping;
	private final ModelMapAdapters.ModelElementIdentity.Factory identityFactory;

	public DefaultKnownElements(ProjectIdentifier projectIdentifier, ObjectFactory objects, ModelMapAdapters.ModelElementIdentity.Factory identityFactory) {
		this.projectIdentifier = projectIdentifier;
		this.knownElements = objects.domainObjectSet(ModelMapAdapters.ModelElementIdentity.class);
		this.mapping = objects.namedDomainObjectSet(KnownElement.class);
		this.identityFactory = identityFactory;
		this.realizableElements = objects.domainObjectSet(RealizableElement.class);

		knownElements.all(identity -> {
			KnownElement element = mapping.findByName(identity.getName());
			if (element == null) {
				element = new KnownElement(identity.getName(), objects);
				mapping.add(element);
			}

			element.identifiers.add(identity);
		});
	}

	private static final class RealizableElement {
		private final ModelObjectIdentity<?> identity;
		private final NamedDomainObjectProvider<?> provider;

		private RealizableElement(ModelObjectIdentity<?> identity, NamedDomainObjectProvider<?> provider) {
			this.identity = identity;
			this.provider = provider;
		}

		public void realizeNow() {
			provider.get();
		}
	}

	private final class MyRealizeListener implements ModelMapAdapters.RealizeListener {
		@Override
		public void onRealize(ModelObjectIdentity<?> identity) {
			realizableElements.matching(it -> it.identity.equals(identity))
				.all(RealizableElement::realizeNow);
		}
	}

	// TODO: Should it really return ModelObject?
	public <S> ModelObject<S> register(ModelObjectIdentity<S> identity, PolymorphicDomainObjectRegistry<? super S> factory) {
		// TODO: assert the identifier is not already known
		knownIdentifiers.add(identity.getIdentifier());
		ModelMapAdapters.ModelElementIdentity legacyIdentity = identityFactory.create(identity, new MyRealizeListener());
		knownElements.add(legacyIdentity);
		final NamedDomainObjectProvider<S> provider = factory.registerIfAbsent(legacyIdentity.getName(), identity.getType().getConcreteType());
		realizableElements.add(new RealizableElement(identity, provider));
		return legacyIdentity.asModelObject(identity.getType().getConcreteType());
	}

	public <S> S create(String name, Class<S> type, Function<? super KnownElement, ? extends S> factory) {
		KnownElement element = mapping.findByName(name);
		if (element == null) {
			knownElements.add(identityFactory.create(ofIdentity(projectIdentifier.child(name), type), new MyRealizeListener()));
			element = Objects.requireNonNull(mapping.findByName(name));
		}

		return factory.apply(element);
	}

	// TODO: How to deal with identity vs element
	public <T> ModelObject<T> getById(ModelObjectIdentifier identifier, Class<T> type) {
		KnownElement element = mapping.findByName(ModelObjectIdentifiers.asFullyQualifiedName(identifier).toString());
		if (element == null) {
			throw new RuntimeException("element not known");
		}

		return element.identifiers.stream().filter(it -> it.getIdentifier().equals(identifier)).findFirst().orElseThrow(() -> new RuntimeException("element with same name found but not same id")).asModelObject(type);
	}

	@Override
	public boolean isKnown(ModelObjectIdentifier identifier, Class<?> type) {
		// Note: we ignore the type here because this instance is a one-to-one match with the ModelMap.
		return knownIdentifiers.contains(identifier);
	}

	@Nullable
	public KnownElement findByName(String name) {
		return mapping.findByName(name);
	}

	public void forEach(Action<? super ModelMapAdapters.ModelElementIdentity> configureAction) {
		knownElements.all(configureAction);
	}

	public static final class KnownElement implements Named {
		private final String name;
		// TODO: Should reduce visibility
		final DomainObjectSet<ModelMapAdapters.ModelElementIdentity> identifiers;

		public KnownElement(String name, ObjectFactory objects) {
			this.name = name;
			this.identifiers = objects.domainObjectSet(ModelMapAdapters.ModelElementIdentity.class);
		}

		@Override
		public String getName() {
			return name;
		}

		public ModelObjectIdentifier getIdentifier() {
			assert identifiers.size() >= 1; // TODO: Need to figure out
			return identifiers.iterator().next().getIdentifier();
		}

		@Override
		public String toString() {
			return "KnownElement '" + name + "': " + identifiers.stream().map(Object::toString).collect(Collectors.joining(","));
		}
	}

	public static final class Factory {
		private final ProjectIdentifier projectIdentifier;
		private final ObjectFactory objects;
		private final ProviderFactory providers;
		private final DiscoveredElements discoveredElements;
		private final Project project;

		private Factory(ProjectIdentifier projectIdentifier, ObjectFactory objects, ProviderFactory providers, DiscoveredElements discoveredElements, Project project) {
			this.projectIdentifier = projectIdentifier;
			this.objects = objects;
			this.providers = providers;
			this.discoveredElements = discoveredElements;
			this.project = project;
		}

		public static Factory forProject(Project project) {
			return new Factory(ProjectIdentifier.of(project), project.getObjects(), project.getProviders(), model(project).getExtensions().getByType(DiscoveredElements.class), project);
		}

		public DefaultKnownElements create(ModelMapAdapters.ModelElementIdentity.ElementProvider elementProvider) {
			return new DefaultKnownElements(projectIdentifier, objects, new ModelMapAdapters.ModelElementIdentity.Factory(providers, elementProvider, discoveredElements, project));
		}
	}
}
