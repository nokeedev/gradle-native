/*
 * Copyright 2021 the original author or authors.
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

import dev.nokee.model.internal.DomainObjectEntities;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.HasBaseName;
import org.gradle.api.provider.Property;

import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;

@DomainObjectEntities.Tag(ModelBackedHasBaseNameMixIn.Tag.class)
public interface ModelBackedHasBaseNameMixIn extends HasBaseName {
	default Property<String> getBaseName() {
		return ModelProperties.of(this, BaseNamePropertyComponent.class).asProperty(property(of(String.class)));
	}

	interface Tag extends ModelTag {}
}
