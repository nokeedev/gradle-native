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

import java.io.File;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public final class ModelPropertyRegistrationFactory {
	public <T> ModelRegistration createProperty(Class<T> type) {
		return ModelRegistration.builder()
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(of(type)))
			.build();
	}

	public <T> ModelRegistration createFileCollectionProperty() {
		return ModelRegistration.builder()
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(set(of(File.class))))
			.build();
	}
}
