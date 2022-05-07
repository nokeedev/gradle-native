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
package dev.nokee.model.internal.core;

import dev.nokee.model.internal.ModelPropertyIdentifier;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public final class ModelPropertyRegistrationFactory {
	private final ObjectFactory objects;

	public ModelPropertyRegistrationFactory(ObjectFactory objects) {
		this.objects = objects;
	}

	public <T> ModelRegistration createProperty(ModelPropertyIdentifier identifier, Class<T> type) {
		val property = objects.property(type);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(of(type)))
			.withComponent(new GradlePropertyComponent(property))
			.withComponent(new ModelElementProviderSourceComponent(property))
			.build();
	}

	public <T> ModelRegistration createFileCollectionProperty(ModelPropertyIdentifier identifier) {
		val property = objects.fileCollection();
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(set(of(File.class))))
			.withComponent(new GradlePropertyComponent(property))
			.withComponent(new ModelElementProviderSourceComponent(property.getElements()))
			.build();
	}
}
