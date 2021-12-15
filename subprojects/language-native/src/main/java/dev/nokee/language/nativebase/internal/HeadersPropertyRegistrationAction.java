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
package dev.nokee.language.nativebase.internal;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.SourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;

@AutoFactory
public final class HeadersPropertyRegistrationAction extends ModelActionWithInputs.ModelAction2<LanguageSourceSetIdentifier, ModelState.IsAtLeastRegistered> {
	private final LanguageSourceSetIdentifier identifier;
	private final ModelRegistry registry;
	private final SourceSetFactory sourceSetFactory;

	HeadersPropertyRegistrationAction(LanguageSourceSetIdentifier identifier, @Provided ModelRegistry registry, @Provided SourceSetFactory sourceSetFactory) {
		this.identifier = identifier;
		this.registry = registry;
		this.sourceSetFactory = sourceSetFactory;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (identifier.equals(this.identifier)) {
			val propertyIdentifier = ModelPropertyIdentifier.of(identifier, "headers");
			val element = registry.register(ModelRegistration.builder()
				.withComponent(DomainObjectIdentifierUtils.toPath(propertyIdentifier))
				.withComponent(propertyIdentifier)
				.withComponent(IsModelProperty.tag())
				.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), sourceSetFactory::sourceSet))
				.build());
			entity.addComponent(new HeadersProperty(ModelNodes.of(element)));
			entity.addComponent(new ProjectHeaderSearchPaths(element.as(SourceSet.class).flatMap(elementsOf(SourceSet::getSourceDirectories))));
		}
	}

}
