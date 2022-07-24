/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.developmentvariant;

import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import org.gradle.api.provider.Property;

import static dev.nokee.model.internal.type.ModelType.of;

@DomainObjectEntities.Tag(HasDevelopmentVariantMixIn.Tag.class)
public interface HasDevelopmentVariantMixIn<T extends Variant> extends HasDevelopmentVariant<T> {
	@Override
	@SuppressWarnings("unchecked")
	default Property<T> getDevelopmentVariant() {
		return (Property<T>) ModelProperties.of(this, DevelopmentVariantPropertyComponent.class).asProperty(of(Property.class));
	}

	interface Tag extends ModelTag {}
}
