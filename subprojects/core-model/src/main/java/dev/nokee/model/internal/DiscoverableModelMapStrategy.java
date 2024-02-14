/*
 * Copyright 2024 the original author or authors.
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

import org.gradle.api.Namer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.Set;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

final class DiscoverableModelMapStrategy<ElementType> extends ForwardingModelMapStrategy<ElementType> {
	private final ModelMapStrategy<ElementType> delegate;

	DiscoverableModelMapStrategy(Namer<ElementType> namer, ObjectFactory objects, DiscoveredElements discoveredElements, ProviderFactory providers, ModelMapStrategy<ElementType> delegate) {
		this.delegate = new ListeningModelMapStrategy<>(namer, objects, discoveredElements, new InstrumentModelMapStrategy<>(discoveredElements, new ForwardingModelMapStrategy<ElementType>() {
			@Override
			protected ModelMapStrategy<ElementType> delegate() {
				return delegate;
			}

			@Override
			public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
				return discoveredElements.discover(identity, () -> {
					return super.register(identity);
				});
			}

			@Override
			public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
				// TODO: Discover identifier
				return super.getById(identifier);
			}

			@Override
			public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
				return providers.provider(() -> {
					discoveredElements.discoverAll(it -> it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(ModelObjectIdentity.ofIdentity(it.getIdentifier(), it.getType())));
					return super.getElements(type, spec);
				}).flatMap(noOpTransformer());
			}
		}));
	}

	@Override
	protected ModelMapStrategy<ElementType> delegate() {
		return delegate;
	}
}
