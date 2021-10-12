/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentVariants;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

public final class NativeApplicationComponentVariants implements ComponentVariants {
	private final VariantCollection<DefaultNativeApplicationVariant> variantCollection;
	private final Property<DefaultNativeApplicationVariant> developmentVariant;

	public NativeApplicationComponentVariants(ObjectFactory objectFactory, DefaultNativeApplicationComponent component, ProviderFactory providerFactory, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository) {
		this.variantCollection = new VariantCollection<>(component.getIdentifier(), DefaultNativeApplicationVariant.class, eventPublisher, viewFactory, variantRepository);
		this.developmentVariant = objectFactory.property(DefaultNativeApplicationVariant.class).convention(providerFactory.provider(new BuildableDevelopmentVariantConvention<>(() -> getVariantCollection().get())));
	}

	public VariantCollection<DefaultNativeApplicationVariant> getVariantCollection() {
		return variantCollection;
	}

	public Property<DefaultNativeApplicationVariant> getDevelopmentVariant() {
		return developmentVariant;
	}
}
