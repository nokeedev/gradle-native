/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.*;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.base.internal.variants.VariantViewInternal;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public final class VariantCollection<T extends Variant> {
	private final Class<T> elementType;
	private final DomainObjectIdentifier owner;
	private final DomainObjectEventPublisher eventPublisher;
	private final VariantViewFactory variantViewFactory;
	private final VariantRepository variantRepository;
	private final VariantViewInternal<T> view;

	// TODO: Make the distinction between public and implementation type
	public VariantCollection(DomainObjectIdentifier owner, Class<T> elementType, DomainObjectEventPublisher eventPublisher, VariantViewFactory variantViewFactory, VariantRepository variantRepository) {
		this.owner = owner;
		this.elementType = elementType;
		this.eventPublisher = eventPublisher;
		this.variantViewFactory = variantViewFactory;
		this.variantRepository = variantRepository;
		this.view = variantViewFactory.create(owner, elementType);
	}

	public VariantProvider<T> registerVariant(VariantIdentifier<T> identifier, VariantFactory<T> factory) {
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
		eventPublisher.publish(new RealizableDomainObjectDiscovered(identifier, () -> {
			val result = factory.create(identifier.getFullName(), (BuildVariantInternal) identifier.getBuildVariant());
			eventPublisher.publish(new DomainObjectCreated<>(identifier, result));
		}));
		Value<T> value = Value.supplied(elementType, () -> variantRepository.get(identifier));
		return new VariantProvider<>(identifier, value);
	}

	// TODO: I don't like that we have to pass in the viewElementType
	public <S extends Variant> VariantViewInternal<S> getAsView(Class<S> viewElementType) {
		Preconditions.checkArgument(viewElementType.isAssignableFrom(elementType), "element type of the view needs to be the same type or a supertype of the element of this collection");
		return variantViewFactory.create(owner, viewElementType);
	}

	public Provider<List<T>> filter(Spec<? super T> spec) {
		return view.filter(spec);
	}

	public void realize() {
		view.get(); // crappy way to realize
	}

	// TODO: This may not be needed, the only place it's used should probably use public API
	public Set<T> get() {
		return (Set<T>) view.getElements().get();
	}

	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		view.whenElementKnown(action);
	}
}
