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
package dev.nokee.language.base.internal;

import com.google.common.collect.Streams;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelRegistration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public final class LanguageSourceSetRegistrationBuilder {
	private final ModelRegistration.Builder builder = ModelRegistration.builder();

	LanguageSourceSetRegistrationBuilder(LanguageSourceSetIdentifier identifier, ModelProjection projection) {
		builder.withComponent(identifier)
			.withComponent(toPath(identifier))
			.withComponent(IsLanguageSourceSet.tag())
			.withComponent(projection)
			.withComponent(managed(of(BaseLanguageSourceSetProjection.class)));
	}

	public ModelRegistration build() {
		return builder.build();
	}

	private static ModelPath toPath(LanguageSourceSetIdentifier identifier) {
		return ModelPath.path(Streams.stream(identifier).flatMap(it -> {
			if (it instanceof ProjectIdentifier) {
				return Stream.empty();
			} else if (it instanceof HasName) {
				return Stream.of(((HasName) it).getName().toString());
			} else {
				throw new UnsupportedOperationException();
			}
		}).collect(Collectors.toList()));
	}
}
