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

import dev.nokee.model.internal.AbstractKnownDomainObject;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import org.gradle.api.provider.Provider;

public final class KnownVariant<T extends Variant> extends AbstractKnownDomainObject<Variant, T> {
	private final VariantIdentifier<T> identifier;

	public KnownVariant(VariantIdentifier<T> identifier, Provider<T> provider, VariantConfigurer configurer) {
		super(identifier, provider, configurer);
		this.identifier = identifier;
	}

	@Override
	public VariantIdentifier<T> getIdentifier() {
		return identifier;
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}
}
