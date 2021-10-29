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

import dev.nokee.internal.Factory;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public final class LanguageSourceSetRegistrationBuilder {
	private final ModelRegistration.Builder builder = ModelRegistration.builder();

	LanguageSourceSetRegistrationBuilder(LanguageSourceSetIdentifier identifier, ModelProjection projection, ModelRegistry registry, Factory<ConfigurableSourceSet> sourceSetFactory) {
		builder.withComponent(identifier)
			.withComponent(toPath(identifier))
			.withComponent(IsLanguageSourceSet.tag())
			.withComponent(projection)
			.withComponent(managed(of(BaseLanguageSourceSetStrategy.class)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(LanguageSourceSetIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), ModelComponentReference.of(ModelPath.class), (entity, id, state, path) -> {
				if (id.equals(identifier)) {
					registry.register(ModelRegistration.builder()
						.withComponent(path.child("source"))
						.withComponent(IsModelProperty.tag())
						.withComponent(createdUsing(of(ConfigurableSourceSet.class), sourceSetFactory))
						.build());
				}
			}))
		;
	}

	public ModelRegistration build() {
		return builder.build();
	}
}
