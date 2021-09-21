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
package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.platform.base.Variant;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;

public final class VariantViewImpl<T extends Variant> extends AbstractDomainObjectView<Variant, T> implements VariantViewInternal<T>, DomainObjectView<T> {
	private final KnownVariantFactory knownVariantFactory;

	VariantViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, VariantRepository repository, VariantConfigurer configurer, VariantViewFactory viewFactory, KnownVariantFactory knownVariantFactory) {
		super(viewOwner, viewElementType, repository, configurer, viewFactory);
		this.knownVariantFactory = knownVariantFactory;
	}

	@Override
	public void configureEach(Closure<Void> closure) {
		DomainObjectView.super.configureEach(closure);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Closure<Void> closure) {
		DomainObjectView.super.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Closure<Void> closure) {
		DomainObjectView.super.configureEach(spec, closure);
	}

	@Override
	public <S extends T> VariantViewImpl<S> withType(Class<S> type) {
		return (VariantViewImpl<S>) super.withType(type);
	}

	@Override
	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		configurer.whenElementKnown(viewOwner, viewElementType, identifier -> action.execute(knownVariantFactory.create(identifier)));
	}

	@Override
	public <S extends T> void whenElementKnown(Class<S> type, Action<? super KnownVariant<S>> action) {
		configurer.whenElementKnown(viewOwner, type, identifier -> action.execute(knownVariantFactory.create(identifier)));
	}
}
