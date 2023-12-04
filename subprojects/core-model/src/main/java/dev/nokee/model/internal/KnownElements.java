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

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public final class KnownElements {
	private final ProjectIdentifier projectIdentifier;
	private final DomainObjectSet<ModelMapAdapters.ModelElementIdentity> knownElements;
	private final NamedDomainObjectSet<KnownElement> mapping;

	public KnownElements(ProjectIdentifier projectIdentifier, ObjectFactory objects) {
		this.projectIdentifier = projectIdentifier;
		this.knownElements = objects.domainObjectSet(ModelMapAdapters.ModelElementIdentity.class);
		this.mapping = objects.namedDomainObjectSet(KnownElement.class);

		knownElements.all(identity -> {
			KnownElement element = mapping.findByName(identity.getName());
			if (element == null) {
				element = new KnownElement(identity.getName(), objects);
				mapping.add(element);
			}

			element.identifiers.add(identity);
		});
	}

	// TODO: Consider using NamedDomainObjectRegistry instead of Function
	// TODO: Should it really return ModelObject?
	public <S> ModelObject<S> register(ModelObjectIdentifier identifier, Class<S> type, Function<? super String, ? extends NamedDomainObjectProvider<S>> factory) {
		ModelMapAdapters.ModelElementIdentity identity = new ModelMapAdapters.ModelElementIdentity(identifier, type);
		knownElements.add(identity);
		identity.attachProvider(factory.apply(identity.getName()));
		return identity.asModelObject(type);
	}

	public <S> S create(String name, Class<S> type, Function<? super KnownElement, ? extends S> factory) {
		KnownElement element = mapping.findByName(name);
		if (element == null) {
			knownElements.add(new ModelMapAdapters.ModelElementIdentity(projectIdentifier.child(name), type));
			element = Objects.requireNonNull(mapping.findByName(name));
		}

		return factory.apply(element);
	}

	// TODO: How to deal with identity vs element
	public ModelMapAdapters.ModelElementIdentity getById(ModelObjectIdentifier identifier) {
		KnownElement element = mapping.findByName(ModelObjectIdentifiers.asFullyQualifiedName(identifier).toString());
		if (element == null) {
			throw new RuntimeException("element not known");
		}

		return element.identifiers.stream().filter(it -> it.getIdentifier().equals(identifier)).findFirst().orElseThrow(() -> new RuntimeException("element with same name found but not same id"));
	}

	@Nullable
	public KnownElement findByName(String name) {
		return mapping.findByName(name);
	}

	public void forEach(Action<? super ModelMapAdapters.ModelElementIdentity> configureAction) {
		knownElements.all(configureAction);
	}

	public <S> Action<S> forCreatedElements(Namer<S> namer, Function<? super String, NamedDomainObjectProvider<?>> query) {
		return it -> {
			KnownElement element = mapping.findByName(namer.determineName(it));
			if (element == null) {
				return; // ignore these elements, they were not explicitly registered by us and are not created by our factories
			}

			element.identifiers.all(t -> {
				if (t.elementProvider == null) {
					t.attachProvider(query.apply(element.getName()));
				}
			});
		};
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
	}

	public static final class Factory implements dev.nokee.internal.Factory<KnownElements> {
		private final ProjectIdentifier projectIdentifier;
		private final ObjectFactory objects;

		private Factory(ProjectIdentifier projectIdentifier, ObjectFactory objects) {
			this.projectIdentifier = projectIdentifier;
			this.objects = objects;
		}

		public static Factory forProject(Project project) {
			return new Factory(ProjectIdentifier.of(project), project.getObjects());
		}

		@Override
		public KnownElements create() {
			return new KnownElements(projectIdentifier, objects);
		}
	}
}
