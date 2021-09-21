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
import dev.nokee.model.internal.AbstractDomainObjectViewFactory;
import dev.nokee.platform.base.Variant;

import static java.util.Objects.requireNonNull;

public final class VariantViewFactory extends AbstractDomainObjectViewFactory<Variant> {
	private final VariantRepository repository;
	private final VariantConfigurer configurer;
	private final KnownVariantFactory knownObjectFactory;

	public VariantViewFactory(VariantRepository repository, VariantConfigurer configurer, KnownVariantFactory knownObjectFactory) {
		super(Variant.class);
		this.repository = repository;
		this.configurer = configurer;
		this.knownObjectFactory = knownObjectFactory;
	}

	@Override
	public <S extends Variant> VariantViewImpl<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType) {
		return new VariantViewImpl<>(requireNonNull(viewOwner), elementType, repository, configurer, this, knownObjectFactory);
	}
}
