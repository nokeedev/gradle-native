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

import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.specs.Spec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.nokee.model.internal.ModelElementActionAdapter.elementWith;

public /*final*/ class DefaultModelObjects implements ModelObjects {
	private final Map<ModelObjectIdentifier, ModelMapAdapters.ModelElementIdentity> identifierToElements = new HashMap<>();
	private final List<ModelMapAdapters.ModelElementIdentity> elements = new ArrayList<>();
	private final ProviderFactory providers;
	private final ObjectFactory objects;
	@SuppressWarnings("rawtypes") private final DomainObjectSet<ModelMap> collections;

	@Inject
	public DefaultModelObjects(ProviderFactory providers, ObjectFactory objects) {
		this.providers = providers;
		this.objects = objects;
		this.collections = objects.domainObjectSet(ModelMap.class);
	}

	@Override
	public <T> void register(ModelMap<T> repository) {
		repository.whenElementKnow(it -> {
			elements.add(it);
			identifierToElements.put(it.getIdentifier(), it);
		});
		collections.add(repository);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void configureEach(BiConsumer<? super ModelObjectIdentity, ? super Object> configureAction) {
		collections.all(it -> it.configureEach(elementWith((identifier, target) ->
			configureAction.accept(create(identifier), target))));
	}

	private ModelObjectIdentity create(ModelObjectIdentifier identifier) {
		return new ModelObjectIdentity() {
			@Override
			public ModelObjectIdentifier getIdentifier() {
				return identifier;
			}

			@Override
			public Optional<ModelObjectIdentity> getParent() {
				return Optional.ofNullable(identifier.getParent()).map(it -> create(it));
			}

			@Override
			public Optional<Object> getAsOptional() {
				return Optional.of(identifierToElements.get(identifier).get()); // TODO: cannot be null
			}

			@Nullable
			@Override
			public Object getOrNull() {
				return identifierToElements.get(identifier).get(); // TODO: Cannot be null
			}

			@Override
			public Stream<ModelObjectIdentity> getParents() {
				return parentsOf(identifier).map(it -> create(it.getIdentifier()));
			}

			@Override
			public boolean instanceOf(Class<?> type) {
				return identifierToElements.get(identifier).instanceOf(type);
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void configureEach(Class<T> type, BiConsumer<? super ModelObjectIdentity, ? super T> configureAction) {
		collections.all(it -> it.configureEach(type, elementWith((identifier, target) ->
			configureAction.accept(create(identifier), type.cast(target)))));
	}

	@Override
	public <T> void configureEach(TypeOf<T> type, BiConsumer<? super ModelObjectIdentity, ? super T> configureAction) {
		configureEach(type.getConcreteClass(), configureAction);
	}

	@Override
	public Stream<ModelMapAdapters.ModelElementIdentity> parentsOf(ModelObjectIdentifier identifier) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ParentIterator(identifier.getParent()), Spliterator.ORDERED), false).filter(it -> !(it instanceof ProjectIdentifier)).map(identifierToElements::get);
	}

	private static final class ParentIterator implements Iterator<ModelObjectIdentifier> {
		@Nullable private ModelObjectIdentifier nextValue;

		public ParentIterator(@Nullable ModelObjectIdentifier nextValue) {
			this.nextValue = nextValue;
		}

		@Override
		public boolean hasNext() {
			return nextValue != null;
		}

		@Override
		public ModelObjectIdentifier next() {
			if (nextValue == null) {
				throw new NoSuchElementException();
			}

			final ModelObjectIdentifier result = nextValue;
			nextValue = nextValue.getParent();
			return result;
		}
	}

	@Override
	public <T> Provider<Set<T>> get(Class<T> type) {
		return providers.provider(() -> {
			final List<ModelMapAdapters.ModelElementIdentity> result = new ArrayList<>();

			forEachIdentity(it -> {
				// TODO: Should provide automatic discovery
				if (it.instanceOf(type)) {
					result.add(it);
				}
			});
			return result;
		}).flatMap(identities -> {
			SetProperty<T> result = objects.setProperty(type);
			identities.forEach(it -> result.add(it.asModelObject(type).asProvider()));
			return result;
		});
	}

	@Override
	public <T> Provider<Set<T>> get(Class<T> type, Spec<? super ModelMapAdapters.ModelElementIdentity> spec) {
		return providers.provider(() -> {
			final List<ModelMapAdapters.ModelElementIdentity> result = new ArrayList<>();

			forEachIdentity(it -> {
				if (spec.isSatisfiedBy(it)) {
					if (it.instanceOf(type)) {
						result.add(it);
					}
				}
			});
			return result;
		}).flatMap(identities -> {
			SetProperty<T> result = objects.setProperty(type);
			identities.forEach(it -> result.add(it.asModelObject(type).asProvider()));
			return result;
		});
	}

	private void forEachIdentity(Consumer<? super ModelMapAdapters.ModelElementIdentity> action) {
		// WARNING: Don't listen to IntelliJ. It's retarded and a index loop is the only way...
		//   Because more elements may appear in the list as they are discovered.
		for (int i = 0; i < elements.size(); ++i) {
			action.accept(elements.get(i));
		}
	}
}
