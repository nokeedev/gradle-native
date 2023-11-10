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

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public /*final*/ class DefaultModelObjects implements ModelObjects {
	private final Map<ModelObjectIdentifier, ModelMapAdapters.ModelElementIdentity> identifierToElements = new HashMap<>();
	private final List<ModelMapAdapters.ModelElementIdentity> elements = new ArrayList<>();
	@SuppressWarnings("rawtypes") private final DomainObjectSet<ModelMap> collections;

	@Inject
	public DefaultModelObjects(ObjectFactory objects) {
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
	public void configureEach(BiConsumer<? super ModelObjectIdentifier, ? super Object> configureAction) {
		collections.all(it -> it.configureEach(target -> {
			ModelElementSupport.safeAsModelElement(target).map(ModelElement::getIdentifier).ifPresent(identifier -> {
				configureAction.accept(identifier, target);
			});
		}));
	}

	@Override
	public Stream<ModelMapAdapters.ModelElementIdentity> parentsOf(ModelObjectIdentifier identifier) {
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
}
