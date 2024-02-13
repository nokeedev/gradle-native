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
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.nokee.model.internal.ModelObjectIdentity.ofIdentity;

public /*final*/ class DefaultModelObjects implements ModelObjects {
	private final Map<ModelObjectIdentifier, KnownModelObject<?>> identifierToElements = new HashMap<>();
	private final List<KnownModelObject<?>> elements = new ArrayList<>();
	private final ProviderFactory providers;
	@SuppressWarnings("rawtypes") private final DomainObjectSet<ModelMap> collections;
	private final SetProviderFactory setProviders;
	private final DiscoveredElements discoveredElements;

	@Inject
	public DefaultModelObjects(ProviderFactory providers, ObjectFactory objects, SetProviderFactory setProviders, DiscoveredElements discoveredElements) {
		this.providers = providers;
		this.collections = objects.domainObjectSet(ModelMap.class);
		this.setProviders = setProviders;
		this.discoveredElements = discoveredElements;
	}

	@Override
	public <T> void register(ModelMap<T> repository) {
		repository.whenElementKnown(it -> {
			elements.add(it);
			identifierToElements.put(it.getIdentifier(), it);
		});
		collections.add(repository);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void configureEach(Action<? super Object> action) {
		collections.all(it -> it.configureEach(action));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void whenElementKnown(Action<? super KnownModelObject<? extends Object>> action) {
		collections.all(it -> it.whenElementKnown(action));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void whenElementFinalized(Action<? super Object> action) {
		collections.all(it -> it.whenElementFinalized(action));
	}

	@Override
	public Stream<KnownModelObject<?>> parentsOf(ModelObjectIdentifier identifier) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ParentIterator(identifier.getParent()), Spliterator.ORDERED), false).map(identifierToElements::get);
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
	@SuppressWarnings("unchecked")
	public <T> Provider<Set<T>> get(Class<T> type) {
		return setProviders.create(builder -> {
			discoveredElements.discoverAll(type);
			forEachIdentity(it -> {
				if (it.getType().isSubtypeOf(type)) {
					builder.add((KnownModelObject<T>) it);
				}
			});
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Provider<Set<T>> get(Class<T> type, Spec<? super ModelObjectIdentity<?>> spec) {
		return setProviders.create(builder -> {
			final Spec<? super ModelObjectIdentity<?>> elementSpec = it -> it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(it);

			discoveredElements.discoverAll(elementSpec);

			forEachIdentity(it -> {
				if (elementSpec.isSatisfiedBy(ofIdentity(it.getIdentifier(), it.getType()))) {
					builder.add((KnownModelObject<T>) it);
				}
			});
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Provider<Set<KnownModelObject<T>>> getElements(Class<T> type, Spec<? super ModelObjectIdentity<?>> spec) {
		return providers.provider(() -> {
			final Spec<? super ModelObjectIdentity<?>> elementSpec = it -> it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(it);

			discoveredElements.discoverAll(elementSpec);

			final Set<KnownModelObject<T>> result = new LinkedHashSet<>();

			forEachIdentity(it -> {
				if (elementSpec.isSatisfiedBy(ofIdentity(it.getIdentifier(), it.getType()))) {
					result.add((KnownModelObject<T>) it);
				}
			});
			return result;
		});
	}

	private void forEachIdentity(Consumer<? super KnownModelObject<?>> action) {
		// WARNING: Don't listen to IntelliJ. It's retarded and a index loop is the only way...
		//   Because more elements may appear in the list as they are discovered.
		for (int i = 0; i < elements.size(); ++i) {
			action.accept(elements.get(i));
		}
	}
}
