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

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.Set;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

final class DiscoverableModelMapStrategy<ElementType> implements ModelMapStrategy<ElementType> {
	private final DiscoveredElements discoveredElements;
	private final ProviderFactory providers;
	private final ModelMapStrategy<ElementType> delegate;

	DiscoverableModelMapStrategy(DiscoveredElements discoveredElements, ProviderFactory providers, ModelMapStrategy<ElementType> delegate) {
		this.discoveredElements = discoveredElements;
		this.providers = providers;
		this.delegate = delegate;
	}

	@Override
	public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
		return discoveredElements.discover(identity, () -> delegate.register(identity));
	}

	@Override
	public ModelObjectRegistry.RegistrableTypes getRegistrableTypes() {
		return delegate.getRegistrableTypes();
	}

	@Override
	public void configureEach(Action<? super ElementType> configureAction) {
		discoveredElements.onRealized(configureAction, a -> delegate.configureEach(a));
	}

	@Override
	public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
		discoveredElements.onKnown(configureAction, a -> delegate.whenElementKnown(a));
	}

	@Override
	public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
		discoveredElements.onFinalized(finalizeAction, a -> delegate.whenElementFinalized(a));
	}

	@Override
	public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
		// TODO: Discover identifier or return a discoverable ModelObject
		return delegate.getById(identifier);
	}

	@Override
	public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
		return providers.provider(() -> {
			discoveredElements.discoverAll(it -> it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(ModelObjectIdentity.ofIdentity(it.getIdentifier(), it.getType())));
			return delegate.getElements(type, spec);
		}).flatMap(noOpTransformer());
	}
}
