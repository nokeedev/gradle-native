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
import org.gradle.api.specs.Spec;

import java.util.Set;

public abstract class ForwardingModelMapStrategy<ElementType> implements ModelMapStrategy<ElementType> {
	protected abstract ModelMapStrategy<ElementType> delegate();

	@Override
	public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
		return delegate().register(identity);
	}

	@Override
	public void configureEach(Action<? super ElementType> configureAction) {
		delegate().configureEach(configureAction);
	}

	@Override
	public void whenElementKnown(Action<? super KnownModelObject<? extends ElementType>> configureAction) {
		delegate().whenElementKnown(configureAction);
	}

	@Override
	public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
		delegate().whenElementFinalized(finalizeAction);
	}

	@Override
	public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
		return delegate().getById(identifier);
	}

	@Override
	public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
		return delegate().getElements(type, spec);
	}
}
